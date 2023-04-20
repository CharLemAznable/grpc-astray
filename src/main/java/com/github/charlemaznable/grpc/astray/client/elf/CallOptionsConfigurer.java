package com.github.charlemaznable.grpc.astray.client.elf;

import com.github.charlemaznable.grpc.astray.client.internal.GRpcCallProxy;
import io.grpc.CallOptions;

public interface CallOptionsConfigurer {

    CallOptions configCallOptions(CallOptions callOptions,
                                  GRpcCallProxy callProxy,
                                  Object[] args);
}
