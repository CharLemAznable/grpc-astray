package com.github.charlemaznable.grpc.astray.server.autoconfigure;

import io.grpc.ServerBuilder;
import lombok.AllArgsConstructor;
import lombok.val;
import org.springframework.boot.autoconfigure.condition.ConditionalOnMissingBean;
import org.springframework.boot.context.properties.bind.Bindable;
import org.springframework.boot.context.properties.bind.Binder;
import org.springframework.boot.context.properties.source.ConfigurationPropertyName;
import org.springframework.context.EnvironmentAware;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.core.env.Environment;

import javax.annotation.Nonnull;
import java.net.InetSocketAddress;
import java.net.SocketAddress;
import java.util.Optional;
import java.util.concurrent.TimeUnit;
import java.util.function.Function;
import java.util.function.IntFunction;

import static com.github.charlemaznable.core.lang.ClzPath.classExists;
import static org.joor.Reflect.on;

@SuppressWarnings({"SpringJavaInjectionPointsAutowiringInspection", "SpringFacetCodeInspection", "rawtypes"})
@Configuration
@ConditionalOnGRpcServerEnabled
public class NettyServerBuilderConfiguration implements EnvironmentAware {

    private static final String PREFER_SHADED_NETTY = "grpc.netty-server.on-collision-prefer-shaded-netty";

    private Environment environment;

    @Override
    public void setEnvironment(@Nonnull Environment environment) {
        this.environment = environment;
    }

    @Bean("nettyServerBuilder")
    @ConditionalOnMissingBean(ServerBuilder.class)
    public ServerBuilder nettyServerBuilderSelector(GRpcServerProperties grpcServerProperties) {
        val nettyShadedConfig = classExists("io.grpc.netty.shaded.io.grpc.netty.NettyServerBuilder");
        val nettyConfig = classExists("io.grpc.netty.NettyServerBuilder");

        if (nettyShadedConfig && nettyConfig) {
            boolean preferShadedNetty = Binder.get(environment)
                    .bind(ConfigurationPropertyName.of(PREFER_SHADED_NETTY),
                            Bindable.of(Boolean.class)).orElse(true);
            return preferShadedNetty
                    ? nettyShadedServerBuilder(grpcServerProperties)
                    : nettyServerBuilder(grpcServerProperties);
        } else if (nettyShadedConfig) return nettyShadedServerBuilder(grpcServerProperties);
        else if (nettyConfig) return nettyShadedServerBuilder(grpcServerProperties);
        else return null;
    }

    private ServerBuilder nettyServerBuilder(GRpcServerProperties grpcServerProperties) {
        return nettyServerBuilder(grpcServerProperties,
                io.grpc.netty.NettyServerBuilder::forAddress,
                io.grpc.netty.NettyServerBuilder::forPort);
    }

    private ServerBuilder nettyShadedServerBuilder(GRpcServerProperties grpcServerProperties) {
        return nettyServerBuilder(grpcServerProperties,
                io.grpc.netty.shaded.io.grpc.netty.NettyServerBuilder::forAddress,
                io.grpc.netty.shaded.io.grpc.netty.NettyServerBuilder::forPort);
    }

    private ServerBuilder nettyServerBuilder(GRpcServerProperties grpcServerProperties,
                                             Function<InetSocketAddress, ServerBuilder> mapper,
                                             IntFunction<ServerBuilder> defaultBuilder) {
        return Optional.ofNullable(grpcServerProperties.getNettyServer()).map(nettyServerProperties -> {
            val builder = Optional.ofNullable(nettyServerProperties.getPrimaryListenAddress())
                    .map(mapper).orElse(defaultBuilder.apply(grpcServerProperties.getRunningPort()));

            Optional.ofNullable(nettyServerProperties.getKeepAliveTime())
                    .ifPresent(t -> builder.keepAliveTime(t.toMillis(), TimeUnit.MILLISECONDS));

            Optional.ofNullable(nettyServerProperties.getKeepAliveTimeout())
                    .ifPresent(t -> builder.keepAliveTimeout(t.toMillis(), TimeUnit.MILLISECONDS));

            Optional.ofNullable(nettyServerProperties.getPermitKeepAliveTime())
                    .ifPresent(t -> builder.permitKeepAliveTime(t.toMillis(), TimeUnit.MILLISECONDS));

            Optional.ofNullable(nettyServerProperties.getMaxConnectionAge())
                    .ifPresent(t -> builder.maxConnectionAge(t.toMillis(), TimeUnit.MILLISECONDS));

            Optional.ofNullable(nettyServerProperties.getMaxConnectionAgeGrace())
                    .ifPresent(t -> builder.maxConnectionAgeGrace(t.toMillis(), TimeUnit.MILLISECONDS));

            Optional.ofNullable(nettyServerProperties.getMaxConnectionIdle())
                    .ifPresent(t -> builder.maxConnectionIdle(t.toMillis(), TimeUnit.MILLISECONDS));

            Optional.ofNullable(nettyServerProperties.getPermitKeepAliveWithoutCalls())
                    .ifPresent(builder::permitKeepAliveWithoutCalls);

            Optional.ofNullable(nettyServerProperties.getMaxInboundMessageSize())
                    .ifPresent(s -> builder.maxInboundMessageSize((int) s.toBytes()));

            Optional.ofNullable(nettyServerProperties.getMaxInboundMetadataSize())
                    .ifPresent(s -> builder.maxInboundMetadataSize((int) s.toBytes()));

            val builderWrapper = new NettyServerBuilderWrapper(builder);

            Optional.ofNullable(nettyServerProperties.getAdditionalListenAddresses())
                    .ifPresent(l -> l.forEach(builderWrapper::addListenAddress));

            Optional.ofNullable(nettyServerProperties.getFlowControlWindow())
                    .ifPresent(builderWrapper::flowControlWindow);

            Optional.ofNullable(nettyServerProperties.getInitialFlowControlWindow())
                    .ifPresent(builderWrapper::initialFlowControlWindow);

            Optional.ofNullable(nettyServerProperties.getMaxConcurrentCallsPerConnection())
                    .ifPresent(builderWrapper::maxConcurrentCallsPerConnection);

            return builderWrapper.serverBuilder;
        }).orElse(ServerBuilder.forPort(grpcServerProperties.getRunningPort()));
    }

    @SuppressWarnings("UnusedReturnValue")
    @AllArgsConstructor
    private static class NettyServerBuilderWrapper {

        private ServerBuilder serverBuilder;

        public NettyServerBuilderWrapper addListenAddress(SocketAddress listenAddress) {
            on(serverBuilder).call("addListenAddress", listenAddress);
            return this;
        }

        public NettyServerBuilderWrapper flowControlWindow(int flowControlWindow) {
            on(serverBuilder).call("flowControlWindow", flowControlWindow);
            return this;
        }

        public NettyServerBuilderWrapper initialFlowControlWindow(int initialFlowControlWindow) {
            on(serverBuilder).call("initialFlowControlWindow", initialFlowControlWindow);
            return this;
        }

        public NettyServerBuilderWrapper maxConcurrentCallsPerConnection(int maxCalls) {
            on(serverBuilder).call("maxConcurrentCallsPerConnection", maxCalls);
            return this;
        }
    }
}
