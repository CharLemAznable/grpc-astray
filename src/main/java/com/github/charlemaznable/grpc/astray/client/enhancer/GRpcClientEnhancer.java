package com.github.charlemaznable.grpc.astray.client.enhancer;

import net.sf.cglib.proxy.Callback;

public interface GRpcClientEnhancer {

    boolean isEnabled(Class<?> clientClass);

    Callback build(Class<?> clientClass, Object clientImpl);

    default int getOrder() {
        return 0;
    }
}
