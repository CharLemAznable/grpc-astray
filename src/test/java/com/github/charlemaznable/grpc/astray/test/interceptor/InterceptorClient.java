package com.github.charlemaznable.grpc.astray.test.interceptor;

import com.github.charlemaznable.grpc.astray.client.GRpcCall;
import com.github.charlemaznable.grpc.astray.client.GRpcClient;
import com.github.charlemaznable.grpc.astray.test.common.EmbeddedChannel;

import java.util.concurrent.Future;

@GRpcClient
@EmbeddedChannel
public interface InterceptorClient {

    @GRpcCall(name = "InterceptorService/Test", ignoreServiceName = true)
    Future<String> test(String req);
}
