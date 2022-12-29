package com.github.charlemaznable.grpc.astray.client.enhancer;

import blossom.common.BlossomElf;
import blossom.enhance.BlossomCglibInterceptor;
import com.github.charlemaznable.core.lang.ClzPath;
import com.github.charlemaznable.core.lang.EasyEnhancer;
import com.github.charlemaznable.core.lang.Reloadable;
import com.github.charlemaznable.grpc.astray.client.internal.GRpcClientDummy;
import com.google.auto.service.AutoService;

@AutoService(GRpcClientEnhancer.class)
public final class BlossomGRpcClientEnhancer implements GRpcClientEnhancer {

    @Override
    public boolean isEnabled(Class<?> clientClass) {
        return ClzPath.classExists("blossom.enhance.BlossomCglibInterceptor")
                && BlossomElf.isFastBlossomAnnotated(clientClass);
    }

    @Override
    public Object build(Class<?> clientClass, Object clientImpl) {
        return EasyEnhancer.create(GRpcClientDummy.class,
                new Class[]{clientClass, Reloadable.class},
                new BlossomCglibInterceptor(clientImpl),
                new Object[]{clientClass});
    }

    @Override
    public int getOrder() {
        return -10;
    }
}
