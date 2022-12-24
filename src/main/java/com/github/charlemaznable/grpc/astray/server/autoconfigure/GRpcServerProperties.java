package com.github.charlemaznable.grpc.astray.server.autoconfigure;

import jakarta.annotation.PostConstruct;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.Setter;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.context.SmartLifecycle;
import org.springframework.util.unit.DataSize;

import java.net.InetSocketAddress;
import java.time.Duration;
import java.util.List;
import java.util.Optional;

@ConfigurationProperties("grpc")
@Getter
@Setter
public class GRpcServerProperties {

    public static final int DEFAULT_GRPC_PORT = 7018;
    private Integer port = null;

    private InvocationProperties invocation;
    private NettyServerProperties nettyServer;
    @Getter(AccessLevel.NONE)
    @Setter(AccessLevel.NONE)
    private volatile Integer runningPort = null;
    private int startUpPhase = SmartLifecycle.DEFAULT_PHASE;
    /**
     * Enables the embedded grpc server.
     */
    private boolean enabled = true;
    /**
     * Number of seconds to wait for preexisting calls to finish before shutting down. A negative
     * value is equivalent to an infinite grace period
     */
    private int shutdownGrace = 0;

    public Integer getRunningPort() {
        if (null == runningPort) {
            synchronized (this) {
                if (null == runningPort) {
                    runningPort = Optional.ofNullable(port)
                            .filter(p -> 0 != p).orElse(DEFAULT_GRPC_PORT);
                }
            }
        }
        return runningPort;
    }

    @PostConstruct
    public void init() {
        Optional.ofNullable(nettyServer)
                .map(NettyServerProperties::getPrimaryListenAddress)
                .ifPresent(a -> port = a.getPort());
    }

    @Getter
    @Setter
    public static class InvocationProperties {

        private Integer interceptorOrder;
    }

    @Getter
    @Setter
    public static class NettyServerProperties {

        private boolean onCollisionPreferShadedNetty;
        private Integer flowControlWindow;
        private Integer initialFlowControlWindow;
        private Integer maxConcurrentCallsPerConnection;
        private Duration keepAliveTime;
        private Duration keepAliveTimeout;
        private Duration maxConnectionAge;
        private Duration maxConnectionAgeGrace;
        private Duration maxConnectionIdle;
        private Duration permitKeepAliveTime;
        private DataSize maxInboundMessageSize;
        private DataSize maxInboundMetadataSize;
        private Boolean permitKeepAliveWithoutCalls;
        /**
         * grpc listen address.
         *
         * <p>If configured, takes precedence over {@code grpc.port} property value. Supported format:
         *
         * <ul>
         * <li>{@code host:port} (if port is less than 1, uses random value)
         * <li>{@code host:} (uses default grpc port, 7018 )
         * </ul>
         */
        private InetSocketAddress primaryListenAddress;
        private List<InetSocketAddress> additionalListenAddresses;
    }
}
