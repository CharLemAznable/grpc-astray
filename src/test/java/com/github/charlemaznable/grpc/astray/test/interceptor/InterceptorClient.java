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

    @GRpcCall(name = "InterceptorService/Test", ignoreServiceName = true)
    rx.Single<String> testRx(String req);

    @GRpcCall(name = "InterceptorService/Test", ignoreServiceName = true)
    io.reactivex.Single<String> testRx2(String req);

    @GRpcCall(name = "InterceptorService/Test", ignoreServiceName = true)
    io.reactivex.rxjava3.core.Single<String> testRx3(String req);
}
