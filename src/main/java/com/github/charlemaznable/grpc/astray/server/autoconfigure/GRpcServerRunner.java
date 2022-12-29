package com.github.charlemaznable.grpc.astray.server.autoconfigure;

import com.github.charlemaznable.grpc.astray.server.GRpcService;
import io.grpc.Server;
import io.grpc.ServerBuilder;
import io.grpc.ServerInterceptor;
import io.grpc.ServerInterceptors;
import io.grpc.ServerServiceDefinition;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import lombok.val;
import org.springframework.beans.factory.BeanCreationException;
import org.springframework.beans.factory.support.RootBeanDefinition;
import org.springframework.context.SmartLifecycle;
import org.springframework.context.support.AbstractApplicationContext;
import org.springframework.core.annotation.AnnotationAwareOrderComparator;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Comparator;
import java.util.Optional;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.function.Consumer;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import static com.github.charlemaznable.core.lang.Condition.checkNotNull;

@SuppressWarnings("rawtypes")
@Slf4j(topic = "grpc.astray.server")
@RequiredArgsConstructor
public final class GRpcServerRunner implements SmartLifecycle {

    private final AtomicBoolean isRunning = new AtomicBoolean(false);
    private final AbstractApplicationContext applicationContext;
    private final GRpcServerProperties gRpcServerProperties;
    private final GRpcServicesRegistry gRpcServicesRegistry;
    private final Consumer<ServerBuilder> configurator;
    private final ServerBuilder serverBuilder;
    private Server server;
    private CountDownLatch latch;

    @Override
    public void start() {
        if (isRunning()) return;
        log.info("Starting gRPC Server ...");
        latch = new CountDownLatch(1);
        try {
            val globalInterceptors = gRpcServicesRegistry.getGlobalInterceptors();
            gRpcServicesRegistry.getBeanNameToServiceMap().forEach((name, serviceWrapper) -> {
                val gRpcServiceAnno = applicationContext.findAnnotationOnBean(name, GRpcService.class);
                serverBuilder.addService(bindInterceptors(
                        serviceWrapper.getServiceDefinition(), gRpcServiceAnno, globalInterceptors));
                log.info("'{}' service has been registered.",
                        serviceWrapper.getServiceDefinition().getServiceDescriptor().getName());
            });
            configurator.accept(serverBuilder);
            server = serverBuilder.build().start();
            isRunning.set(true);
            startDaemonAwaitThread();
            log.info("gRPC Server started, listening on port {}.", server.getPort());
            applicationContext.publishEvent(new GRpcServerInitializedEvent(applicationContext));
        } catch (Exception e) {
            throw new IllegalStateException("Failed to start GRPC server", e);
        }
    }

    @Override
    public void stop() {
        Optional.ofNullable(server).ifPresent(s -> {
            log.info("Shutting down gRPC server ...");
            s.shutdown();
            int shutdownGrace = gRpcServerProperties.getShutdownGrace();
            try {
                // If shutdownGrace is 0, then don't call awaitTermination
                if (shutdownGrace < 0) {
                    s.awaitTermination();
                } else if (shutdownGrace > 0) {
                    s.awaitTermination(shutdownGrace, TimeUnit.SECONDS);
                }
            } catch (InterruptedException e) {
                log.error("gRPC server interrupted during destroy.", e);
                Thread.currentThread().interrupt();
            } finally {
                latch.countDown();
            }
            log.info("gRPC server stopped.");
        });
    }

    @Override
    public boolean isRunning() {
        return isRunning.get();
    }

    @Override
    public int getPhase() {
        return gRpcServerProperties.getStartUpPhase();
    }

    private void startDaemonAwaitThread() {
        val awaitThread = new Thread(() -> {
            try {
                latch.await();
            } catch (InterruptedException e) {
                log.error("gRPC server awaiter interrupted.", e);
                Thread.currentThread().interrupt();
            } finally {
                isRunning.set(false);
            }
        });
        awaitThread.setName("grpc-server-awaiter");
        awaitThread.setDaemon(false);
        awaitThread.start();
    }

    private ServerServiceDefinition bindInterceptors(ServerServiceDefinition serviceDefinition,
                                                     GRpcService gRpcServiceAnno,
                                                     Collection<ServerInterceptor> globalInterceptors) {
        checkNotNull(gRpcServiceAnno);
        val privateInterceptors = Stream.of(gRpcServiceAnno.interceptors())
                .map(interceptorClass -> {
                    try {
                        return 0 < applicationContext.getBeanNamesForType(interceptorClass).length
                                ? applicationContext.getBean(interceptorClass)
                                : interceptorClass.getConstructor().newInstance();
                    } catch (Exception e) {
                        throw new BeanCreationException("Failed to create interceptor instance.", e);
                    }
                });
        val interceptors = Stream.concat(gRpcServiceAnno.applyGlobalInterceptors()
                ? globalInterceptors.stream() : Stream.empty(), privateInterceptors)
                .distinct().sorted(serverInterceptorOrderComparator())
                .collect(Collectors.toList());
        return ServerInterceptors.intercept(serviceDefinition, interceptors);
    }

    private Comparator<Object> serverInterceptorOrderComparator() {
        return new AnnotationAwareOrderComparator().withSourceProvider(o -> {
            val sources = new ArrayList<>(2);
            val rootBeanDefinition = Stream.of(applicationContext.getBeanNamesForType(o.getClass()))
                    .findFirst().map(name -> applicationContext.getBeanFactory().getBeanDefinition(name))
                    .filter(RootBeanDefinition.class::isInstance)
                    .map(RootBeanDefinition.class::cast);
            rootBeanDefinition.map(RootBeanDefinition::getResolvedFactoryMethod)
                    .ifPresent(sources::add);
            rootBeanDefinition.map(RootBeanDefinition::getTargetType)
                    .filter(t -> t != o.getClass())
                    .ifPresent(sources::add);
            return sources.toArray();
        }).reversed();
    }
}
