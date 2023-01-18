package com.github.charlemaznable.grpc.astray.client.enhancer;

import blossom.common.BlossomElf;
import blossom.enhance.BlossomBuddyInterceptor;
import com.github.charlemaznable.core.lang.BuddyEnhancer;
import com.github.charlemaznable.core.lang.ClzPath;
import com.github.charlemaznable.core.lang.Reloadable;
import com.github.charlemaznable.grpc.astray.client.internal.GRpcClientDummy;
import com.google.auto.service.AutoService;

@AutoService(GRpcClientEnhancer.class)
public final class BlossomGRpcClientEnhancer implements GRpcClientEnhancer {

    @Override
    public boolean isEnabled(Class<?> clientClass) {
        return ClzPath.classExists("blossom.enhance.BlossomBuddyInterceptor")
                && BlossomElf.isFastBlossomAnnotated(clientClass);
    }

    @Override
    public Object build(Class<?> clientClass, Object clientImpl) {
        return BuddyEnhancer.create(GRpcClientDummy.class,
                new Object[]{clientClass},
                new Class[]{clientClass, Reloadable.class},
                new BlossomBuddyInterceptor(clientImpl));
    }

    @Override
    public int getOrder() {
        return -10;
    }
}
