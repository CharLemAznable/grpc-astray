package com.github.charlemaznable.grpc.astray.client.enhancer;

import com.github.bingoohuang.westcache.cglib.CglibCacheMethodInterceptor;
import com.github.bingoohuang.westcache.utils.Anns;
import com.github.charlemaznable.core.lang.ClzPath;
import com.google.auto.service.AutoService;
import net.sf.cglib.proxy.Callback;

@AutoService(GRpcClientEnhancer.class)
public class WestCacheableGRpcClientEnhancer implements GRpcClientEnhancer {

    @Override
    public boolean isEnabled(Class<?> clientClass) {
        return ClzPath.classExists("com.github.bingoohuang.westcache.cglib.CglibCacheMethodInterceptor")
                && Anns.isFastWestCacheAnnotated(clientClass);
    }

    @Override
    public Callback build(Class<?> clientClass, Object clientImpl) {
        return new CglibCacheMethodInterceptor(clientImpl);
    }
}
