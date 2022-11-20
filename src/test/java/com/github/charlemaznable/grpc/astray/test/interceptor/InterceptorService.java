package com.github.charlemaznable.grpc.astray.test.interceptor;

import com.github.charlemaznable.grpc.astray.server.GRpcMethod;
import com.github.charlemaznable.grpc.astray.server.GRpcService;

@GRpcService(interceptors = {InterceptorChanger.class, InterceptorLogger.class})
public class InterceptorService {

    @GRpcMethod("Test")
    public String test(String req) {
        return "response: " + req + ", ^_^";
    }
}
