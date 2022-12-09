package com.github.charlemaznable.grpc.astray.client.enhancer;

import blossom.cglib.BlossomCglibInterceptor;
import blossom.common.BlossomElf;
import com.github.charlemaznable.core.lang.ClzPath;
import com.google.auto.service.AutoService;
import net.sf.cglib.proxy.Callback;

@AutoService(GRpcClientEnhancer.class)
public final class BlossomGRpcClientEnhancer implements GRpcClientEnhancer {

    @Override
    public boolean isEnabled(Class<?> clientClass) {
        return ClzPath.classExists("blossom.cglib.BlossomCglibInterceptor")
                && BlossomElf.isFastBlossomAnnotated(clientClass);
    }

    @Override
    public Callback build(Class<?> clientClass, Object clientImpl) {
        return new BlossomCglibInterceptor(clientImpl);
    }

    @Override
    public int getOrder() {
        return -10;
    }
}
