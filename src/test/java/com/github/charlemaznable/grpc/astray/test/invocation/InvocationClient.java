package com.github.charlemaznable.grpc.astray.test.invocation;

import com.github.charlemaznable.grpc.astray.client.GRpcCall;
import com.github.charlemaznable.grpc.astray.client.GRpcClient;
import com.github.charlemaznable.grpc.astray.test.common.EmbeddedChannel;

@GRpcClient("Invocation")
@EmbeddedChannel
public interface InvocationClient {

    @GRpcCall("Call")
    InvocationResponse call(InvocationRequest req);
}
