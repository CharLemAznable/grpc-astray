package com.github.charlemaznable.grpc.astray.client;

import com.github.bingoohuang.westcache.cglib.CglibCacheMethodInterceptor;
import com.github.bingoohuang.westcache.utils.Anns;
import com.github.charlemaznable.core.context.FactoryContext;
import com.github.charlemaznable.core.lang.ClzPath;
import com.github.charlemaznable.core.lang.EasyEnhancer;
import com.github.charlemaznable.core.lang.Factory;
import com.github.charlemaznable.grpc.astray.client.internal.GRpcClientDummy;
import com.github.charlemaznable.grpc.astray.client.internal.GRpcClientProxy;
import com.google.common.cache.LoadingCache;
import lombok.NoArgsConstructor;
import net.sf.cglib.proxy.Callback;
import net.sf.cglib.proxy.Enhancer;
import net.sf.cglib.proxy.NoOp;

import javax.annotation.Nonnull;

import static com.github.charlemaznable.core.lang.Condition.checkNotNull;
import static com.github.charlemaznable.core.lang.LoadingCachee.get;
import static com.github.charlemaznable.core.lang.LoadingCachee.simpleCache;
import static com.github.charlemaznable.core.spring.SpringFactory.springFactory;
import static com.google.common.cache.CacheLoader.from;
import static lombok.AccessLevel.PRIVATE;

@NoArgsConstructor(access = PRIVATE)
public final class GRpcFactory {

    private static LoadingCache<Factory, GRpcLoader> loaderCache
            = simpleCache(from(GRpcLoader::new));

    public static <T> T getClient(Class<T> clazz) {
        return grpcLoader(FactoryContext.get()).getClient(clazz);
    }

    public static GRpcLoader springGRpcLoader() {
        return grpcLoader(springFactory());
    }

    public static GRpcLoader grpcLoader(Factory factory) {
        return get(loaderCache, factory);
    }

    @SuppressWarnings("unchecked")
    public static class GRpcLoader {

        private Factory factory;
        private LoadingCache<Class, Object> cache
                = simpleCache(from(this::loadClient));

        GRpcLoader(Factory factory) {
            this.factory = checkNotNull(factory);
        }

        public <T> T getClient(Class<T> clazz) {
            return (T) get(cache, clazz);
        }

        @Nonnull
        private <T> Object loadClient(@Nonnull Class<T> clazz) {
            ensureClassIsAnInterface(clazz);
            return wrapWestCacheable(clazz,
                    EasyEnhancer.create(GRpcClientDummy.class,
                            new Class[]{clazz},
                            method -> {
                                if (method.isDefault()) return 1;
                                return 0;
                            }, new Callback[]{
                                    new GRpcClientProxy(clazz, factory),
                                    NoOp.INSTANCE}, null));
        }

        private <T> void ensureClassIsAnInterface(Class<T> clazz) {
            if (clazz.isInterface()) return;
            throw new GRpcClientException(clazz + " is not An Interface");
        }

        private <T> Object wrapWestCacheable(Class<T> clazz, Object impl) {
            if (ClzPath.classExists("com.github.bingoohuang.westcache.cglib.CglibCacheMethodInterceptor")
                    && Anns.isFastWestCacheAnnotated(clazz)) {
                return Enhancer.create(GRpcClientDummy.class, new Class[]{clazz},
                        new CglibCacheMethodInterceptor(impl));
            }
            return impl;
        }
    }
}
