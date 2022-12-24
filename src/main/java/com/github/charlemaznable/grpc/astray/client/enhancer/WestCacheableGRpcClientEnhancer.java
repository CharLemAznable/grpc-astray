package com.github.charlemaznable.grpc.astray.client.enhancer;

import com.github.bingoohuang.westcache.cglib.CglibCacheMethodInterceptor;
import com.github.bingoohuang.westcache.utils.Anns;
import com.github.charlemaznable.core.lang.ClzPath;
import com.github.charlemaznable.core.lang.EasyEnhancer;
import com.github.charlemaznable.core.lang.Reloadable;
import com.github.charlemaznable.grpc.astray.client.internal.GRpcClientDummy;
import com.google.auto.service.AutoService;

@AutoService(GRpcClientEnhancer.class)
public final class WestCacheableGRpcClientEnhancer implements GRpcClientEnhancer {

    @Override
    public boolean isEnabled(Class<?> clientClass) {
        return ClzPath.classExists("org.springframework.cglib.proxy.Enhancer")
                && ClzPath.classExists("com.github.bingoohuang.westcache.cglib.CglibCacheMethodInterceptor")
                && Anns.isFastWestCacheAnnotated(clientClass);
    }

    @Override
    public Object build(Class<?> clientClass, Object clientImpl) {
        return EasyEnhancer.create(GRpcClientDummy.class,
                new Class[]{clientClass, Reloadable.class},
                new CglibCacheMethodInterceptor(clientImpl),
                new Object[]{clientClass});
    }
}
