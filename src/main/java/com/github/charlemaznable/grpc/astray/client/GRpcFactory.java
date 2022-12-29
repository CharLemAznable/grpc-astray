package com.github.charlemaznable.grpc.astray.client;

import com.github.charlemaznable.core.context.FactoryContext;
import com.github.charlemaznable.core.lang.EasyEnhancer;
import com.github.charlemaznable.core.lang.Factory;
import com.github.charlemaznable.core.lang.Reloadable;
import com.github.charlemaznable.grpc.astray.client.enhancer.GRpcClientEnhancer;
import com.github.charlemaznable.grpc.astray.client.internal.GRpcClientDummy;
import com.github.charlemaznable.grpc.astray.client.internal.GRpcClientProxy;
import com.google.common.cache.LoadingCache;
import lombok.NoArgsConstructor;
import lombok.val;
import net.sf.cglib.proxy.Callback;
import net.sf.cglib.proxy.NoOp;

import javax.annotation.Nonnull;
import java.util.Comparator;
import java.util.List;
import java.util.ServiceLoader;
import java.util.stream.Collectors;
import java.util.stream.StreamSupport;

import static com.github.charlemaznable.core.lang.Condition.checkNotNull;
import static com.github.charlemaznable.core.lang.LoadingCachee.get;
import static com.github.charlemaznable.core.lang.LoadingCachee.simpleCache;
import static com.github.charlemaznable.core.spring.SpringFactory.springFactory;
import static com.google.common.cache.CacheLoader.from;
import static lombok.AccessLevel.PRIVATE;

@NoArgsConstructor(access = PRIVATE)
public final class GRpcFactory {

    private static final LoadingCache<Factory, GRpcLoader> loaderCache
            = simpleCache(from(GRpcLoader::new));
    private static final List<GRpcClientEnhancer> enhancers;

    static {
        enhancers = StreamSupport
                .stream(ServiceLoader.load(GRpcClientEnhancer.class).spliterator(), false)
                .sorted(Comparator.comparingInt(GRpcClientEnhancer::getOrder).reversed())
                .collect(Collectors.toList());
    }

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

        private final Factory factory;
        private final LoadingCache<Class<?>, Object> cache
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
            return wrapWithEnhancer(clazz,
                    EasyEnhancer.create(GRpcClientDummy.class,
                            new Class[]{clazz, Reloadable.class},
                            method -> {
                                if (method.isDefault() || method.getDeclaringClass()
                                        .equals(GRpcClientDummy.class)) return 1;
                                return 0;
                            }, new Callback[]{
                                    new GRpcClientProxy(clazz, factory),
                                    NoOp.INSTANCE
                            }, new Object[]{clazz}));
        }

        private <T> void ensureClassIsAnInterface(Class<T> clazz) {
            if (clazz.isInterface()) return;
            throw new GRpcClientException(clazz + " is not An Interface");
        }

        private <T> Object wrapWithEnhancer(Class<T> clazz, Object impl) {
            Object enhancedImpl = impl;
            for (val enhancer : enhancers) {
                if (enhancer.isEnabled(clazz)) {
                    enhancedImpl = enhancer.build(clazz, enhancedImpl);
                }
            }
            return enhancedImpl;
        }
    }
}
