package com.github.charlemaznable.grpc.astray.client.enhancer;

public interface GRpcClientEnhancer {

    boolean isEnabled(Class<?> clientClass);

    Object build(Class<?> clientClass, Object clientImpl);

    default int getOrder() {
        return 0;
    }
}
