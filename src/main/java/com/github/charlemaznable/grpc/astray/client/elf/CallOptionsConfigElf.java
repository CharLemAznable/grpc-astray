package com.github.charlemaznable.grpc.astray.client.elf;

import com.github.charlemaznable.grpc.astray.client.internal.GRpcCallProxy;
import io.grpc.CallOptions;
import lombok.NoArgsConstructor;
import lombok.val;

import java.util.ServiceLoader;

import static lombok.AccessLevel.PRIVATE;

@NoArgsConstructor(access = PRIVATE)
public final class CallOptionsConfigElf {

    private static final ServiceLoader<CallOptionsConfigurer> configurers;

    static {
        configurers = ServiceLoader.load(CallOptionsConfigurer.class);
    }

    public static CallOptions configCallOptions(CallOptions callOptions,
                                                GRpcCallProxy callProxy,
                                                Object[] args) {
        CallOptions result = callOptions;
        for (val configurer : configurers) {
            result = configurer.configCallOptions(result, callProxy, args);
        }
        return result;
    }
}
