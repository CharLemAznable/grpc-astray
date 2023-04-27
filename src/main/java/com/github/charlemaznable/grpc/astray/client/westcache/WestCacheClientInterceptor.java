package com.github.charlemaznable.grpc.astray.client.westcache;

import com.github.charlemaznable.core.lang.LoadingCachee;
import com.github.charlemaznable.httpclient.westcache.WestCacheContext;
import com.google.common.cache.LoadingCache;
import com.google.common.collect.Maps;
import io.grpc.CallOptions;
import io.grpc.Channel;
import io.grpc.ClientCall;
import io.grpc.ClientInterceptor;
import io.grpc.ForwardingClientCall;
import io.grpc.ForwardingClientCallListener;
import io.grpc.Metadata;
import io.grpc.MethodDescriptor;
import io.grpc.Status;
import lombok.extern.slf4j.Slf4j;
import lombok.val;

import java.util.Map;
import java.util.Optional;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicBoolean;

import static com.github.charlemaznable.core.lang.Await.await;
import static com.github.charlemaznable.grpc.astray.client.westcache.WestCacheCallOptionsConfigurer.WEST_CACHE_CONTEXT_KEY;
import static com.google.common.cache.CacheBuilder.newBuilder;
import static com.google.common.cache.CacheLoader.from;
import static java.util.Objects.isNull;
import static java.util.Objects.nonNull;

@Slf4j
public final class WestCacheClientInterceptor implements ClientInterceptor {

    private final LoadingCache<WestCacheContext, Optional<?>> localCache;
    private final Map<WestCacheContext, WestCacheContext> lockMap = Maps.newConcurrentMap();

    public WestCacheClientInterceptor() {
        this(2 ^ 8, 60);
    }

    public WestCacheClientInterceptor(long localMaximumSize, long localExpireSeconds) {
        this.localCache = newBuilder()
                .maximumSize(localMaximumSize)
                .expireAfterWrite(localExpireSeconds, TimeUnit.SECONDS)
                .build(from(context -> {
                    val cachedItem = context.cacheGet();
                    if (nonNull(cachedItem) && cachedItem.getObject().isPresent()) {
                        // westcache命中, 且缓存非空
                        return Optional.of(cachedItem.getObject().get());
                    }
                    return Optional.empty();
                }));
    }

    @Override
    public <Q, R> ClientCall<Q, R> interceptCall(MethodDescriptor<Q, R> method, CallOptions callOptions, Channel next) {
        val clientCall = next.newCall(method, callOptions);
        val context = callOptions.getOption(WEST_CACHE_CONTEXT_KEY);
        if (isNull(context)) return clientCall;
        return new WestCacheClientCall<>(clientCall, context);
    }

    private final class WestCacheClientCall<Q, R> extends ForwardingClientCall.SimpleForwardingClientCall<Q, R> {

        WestCacheContext context;
        WestCacheClientCallListener<R> callListener;

        WestCacheClientCall(ClientCall<Q, R> delegate,
                            WestCacheContext context) {
            super(delegate);
            this.context = context;
        }

        @Override
        public void start(Listener<R> responseListener, Metadata headers) {
            callListener = new WestCacheClientCallListener<>(responseListener, context);
            super.start(callListener, headers);
        }

        @SuppressWarnings("unchecked")
        @Override
        public void sendMessage(Q message) {
            val cachedOptional = LoadingCachee.get(localCache, context);
            if (cachedOptional.isPresent()) {
                callListener.cachedMessage.set(true);
                try {
                    callListener.onMessage((R) cachedOptional.get());
                } finally {
                    callListener.onClose(Status.OK, new Metadata());
                }
            } else {
                if (isNull(lockMap.putIfAbsent(context, context))) {
                    super.sendMessage(message);
                } else {
                    await(10);
                    sendMessage(message);
                }
            }
        }
    }

    private final class WestCacheClientCallListener<R>
            extends ForwardingClientCallListener.SimpleForwardingClientCallListener<R> {

        WestCacheContext context;
        AtomicBoolean cachedMessage = new AtomicBoolean();

        WestCacheClientCallListener(ClientCall.Listener<R> delegate,
                                    WestCacheContext context) {
            super(delegate);
            this.context = context;
        }

        @Override
        public void onMessage(R message) {
            try {
                if (!cachedMessage.get()) {
                    localCache.put(context, Optional.of(message));
                    context.cachePut(message);
                }
            } catch (Exception e) {
                log.warn("Cache Writing with Error: ", e);
            } finally {
                super.onMessage(message);
            }
        }

        @Override
        public void onClose(Status status, Metadata trailers) {
            lockMap.remove(context);
            super.onClose(status, trailers);
        }
    }
}
