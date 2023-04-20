package com.github.charlemaznable.grpc.astray.client.westcache;

import com.github.bingoohuang.westcache.utils.WestCacheOption;
import com.github.charlemaznable.grpc.astray.client.elf.CallOptionsConfigurer;
import com.github.charlemaznable.grpc.astray.client.internal.GRpcCallProxy;
import com.github.charlemaznable.httpclient.westcache.WestCacheConstant;
import com.github.charlemaznable.httpclient.westcache.WestCacheContext;
import com.google.auto.service.AutoService;
import io.grpc.CallOptions;
import lombok.val;

import static java.util.Objects.nonNull;

@AutoService(CallOptionsConfigurer.class)
public final class WestCacheCallOptionsConfigurer implements CallOptionsConfigurer {

    static final CallOptions.Key<WestCacheContext> WEST_CACHE_CONTEXT_KEY
            = CallOptions.Key.create("WestCacheContext");

    @Override
    public CallOptions configCallOptions(CallOptions callOptions,
                                         GRpcCallProxy callProxy,
                                         Object[] args) {
        // westcache supported
        if (WestCacheConstant.HAS_WESTCACHE) {
            val method = callProxy.method();
            val option = WestCacheOption.parseWestCacheable(method);
            if (nonNull(option)) {
                val cacheKey = option.getKeyer().getCacheKey(
                        option, method, callProxy.proxy(), args);
                return callOptions.withOption(WEST_CACHE_CONTEXT_KEY,
                        new WestCacheContext(option, cacheKey));
            }
        }
        return callOptions;
    }
}
