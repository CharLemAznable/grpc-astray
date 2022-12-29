package com.github.charlemaznable.grpc.astray.server.autoconfigure;

import com.github.charlemaznable.core.lang.Mapp;
import com.github.charlemaznable.grpc.astray.server.GRpcGlobalInterceptor;
import com.github.charlemaznable.grpc.astray.server.GRpcMethod;
import com.github.charlemaznable.grpc.astray.server.GRpcService;
import io.grpc.MethodDescriptor;
import io.grpc.MethodDescriptor.MethodType;
import io.grpc.ServerInterceptor;
import io.grpc.ServerServiceDefinition;
import io.grpc.ServiceDescriptor;
import io.grpc.stub.ServerCalls.UnaryMethod;
import io.grpc.stub.StreamObserver;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.SneakyThrows;
import lombok.extern.slf4j.Slf4j;
import lombok.val;
import org.springframework.beans.factory.InitializingBean;
import org.springframework.context.ApplicationContext;
import org.springframework.context.ApplicationContextAware;
import org.springframework.util.Assert;
import org.springframework.util.function.SingletonSupplier;

import javax.annotation.Nonnull;
import java.lang.reflect.Method;
import java.util.Collection;
import java.util.Map;
import java.util.function.Function;
import java.util.function.Supplier;

import static com.github.charlemaznable.core.lang.Condition.blankThen;
import static com.github.charlemaznable.core.lang.Str.isBlank;
import static com.github.charlemaznable.grpc.astray.common.GRpcAstrayMarshaller.jsonMarshaller;
import static com.github.charlemaznable.grpc.astray.server.autoconfigure.GRpcServicesRegistry.Elf.buildServerServiceDefinition;
import static io.grpc.stub.ServerCalls.asyncUnaryCall;
import static java.util.Objects.isNull;
import static lombok.AccessLevel.PRIVATE;
import static org.springframework.core.annotation.AnnotatedElementUtils.getMergedAnnotation;
import static org.springframework.util.ClassUtils.getShortName;
import static org.springframework.util.ReflectionUtils.invokeMethod;
import static org.springframework.util.ReflectionUtils.makeAccessible;
import static org.springframework.util.StringUtils.capitalize;

public final class GRpcServicesRegistry implements InitializingBean, ApplicationContextAware {

    @AllArgsConstructor
    @Getter
    public static class GRpcServiceWrapper {

        private Object serviceBean;
        private ServerServiceDefinition serviceDefinition;
    }

    private ApplicationContext applicationContext;
    private Supplier<Map<String, GRpcServiceWrapper>> beanNameToService;
    private Supplier<Map<String, GRpcServiceWrapper>> serviceNameToService;
    private Supplier<Collection<ServerInterceptor>> grpcGlobalInterceptors;

    @Override
    public void setApplicationContext(@Nonnull ApplicationContext applicationContext) {
        this.applicationContext = applicationContext;
    }

    @Override
    public void afterPropertiesSet() {
        beanNameToService = SingletonSupplier.of(() ->
                applicationContext.getBeansWithAnnotation(GRpcService.class).entrySet().stream()
                        .collect(Mapp.toMap(Map.Entry::getKey, e -> new GRpcServiceWrapper(
                                e.getValue(), buildServerServiceDefinition(e.getValue())))));

        serviceNameToService = SingletonSupplier.of(() ->
                beanNameToService.get().values().stream().collect(Mapp.toMap(w ->
                        w.serviceDefinition.getServiceDescriptor().getName(), Function.identity())));

        grpcGlobalInterceptors = SingletonSupplier.of(() ->
                applicationContext.getBeansWithAnnotation(GRpcGlobalInterceptor.class).entrySet().stream()
                        .filter(e -> e.getValue() instanceof ServerInterceptor)
                        .collect(Mapp.toMap(Map.Entry::getKey, e -> (ServerInterceptor) e.getValue())).values());
    }

    public Map<String, GRpcServiceWrapper> getBeanNameToServiceMap() {
        return beanNameToService.get();
    }

    public Map<String, GRpcServiceWrapper> getServiceNameToServiceMap() {
        return serviceNameToService.get();
    }

    public Collection<ServerInterceptor> getGlobalInterceptors() {
        return grpcGlobalInterceptors.get();
    }

    @NoArgsConstructor(access = PRIVATE)
    static class Elf {

        static ServerServiceDefinition buildServerServiceDefinition(Object grpcServiceImpl) {
            val grpcServiceClass = grpcServiceImpl.getClass();
            val grpcServiceName = checkGRpcServiceName(grpcServiceClass);
            val grpcServiceMethods = checkGRpcServiceMethods(grpcServiceClass, grpcServiceName);
            val builder = ServerServiceDefinition.builder(
                    buildServiceDescriptor(grpcServiceName, grpcServiceMethods));
            grpcServiceMethods.forEach((method, methodDescriptor) ->
                    builder.addMethod(methodDescriptor, asyncUnaryCall(new MethodHandlers(
                            grpcServiceImpl, method, methodDescriptor.getFullMethodName()))));
            return builder.build();
        }

        private static String checkGRpcServiceName(Class<?> clazz) {
            val grpcServiceAnno = getMergedAnnotation(clazz, GRpcService.class);
            Assert.notNull(grpcServiceAnno, () -> clazz.getName() + " has no GRpcService annotation");
            return blankThen(grpcServiceAnno.value(), () -> getShortName(clazz));
        }

        private static Map<Method, MethodDescriptor<Object, Object>> checkGRpcServiceMethods(Class<?> clazz,
                                                                                             String grpcServiceName) {
            val result = Mapp.<Method, MethodDescriptor<Object, Object>>newHashMap();
            val clazzMethods = clazz.getDeclaredMethods();
            for (val clazzMethod : clazzMethods) {
                val grpcMethodName = checkGRpcMethodName(clazzMethod, grpcServiceName);
                if (isBlank(grpcMethodName)) continue;
                result.put(clazzMethod, MethodDescriptor.newBuilder()
                        .setType(MethodType.UNARY)
                        .setFullMethodName(grpcMethodName)
                        .setSampledToLocalTracing(true)
                        .setRequestMarshaller(jsonMarshaller(clazzMethod.getParameterTypes()[0]))
                        .setResponseMarshaller(jsonMarshaller(clazzMethod.getReturnType()))
                        .build());
            }
            return result;
        }

        private static String checkGRpcMethodName(Method method, String grpcServiceName) {
            val returnType = method.getReturnType();
            if (void.class == returnType || Void.class == returnType) return null;
            val parameters = method.getParameters();
            if (1 != parameters.length) return null;
            val grpcMethodAnno = getMergedAnnotation(method, GRpcMethod.class);
            if (isNull(grpcMethodAnno)) return null;
            val methodName = blankThen(grpcMethodAnno.value(), () -> capitalize(method.getName()));
            return grpcMethodAnno.ignoreServiceName() ? methodName : grpcServiceName + "/" + methodName;
        }

        private static ServiceDescriptor buildServiceDescriptor(String grpcServiceName,
                                                                Map<Method, MethodDescriptor<Object, Object>> grpcServiceMethods) {
            val builder = ServiceDescriptor.newBuilder(grpcServiceName);
            grpcServiceMethods.values().forEach(builder::addMethod);
            return builder.build();
        }
    }

    @Slf4j(topic = "grpc.service.method.handler")
    static class MethodHandlers implements UnaryMethod<Object, Object> {

        private final Object serviceImpl;
        private final Method method;
        private final String fullMethodName;

        MethodHandlers(Object serviceImpl, Method method, String fullMethodName) {
            this.serviceImpl = serviceImpl;
            this.method = method;
            makeAccessible(this.method);
            this.fullMethodName = fullMethodName;
        }

        @SneakyThrows
        @Override
        public void invoke(Object request, StreamObserver<Object> responseObserver) {
            if (log.isDebugEnabled()) {
                log.debug("{} handle request: {}", fullMethodName, request);
            }
            val response = invokeMethod(method, serviceImpl, request);
            responseObserver.onNext(response);
            responseObserver.onCompleted();
            if (log.isDebugEnabled()) {
                log.debug("{} handle response: {}", fullMethodName, response);
            }
        }
    }
}
