package com.github.charlemaznable.grpc.astray.test.common;

import com.github.charlemaznable.grpc.astray.client.enhancer.GRpcClientEnhancer;
import com.google.auto.service.AutoService;

@AutoService(GRpcClientEnhancer.class)
public class TestGRpcClientDisabledEnhancer implements GRpcClientEnhancer {

    @Override
    public boolean isEnabled(Class<?> clientClass) {
        return false;
    }

    @Override
    public Object build(Class<?> clientClass, Object clientImpl) {
        return null;
    }
}
