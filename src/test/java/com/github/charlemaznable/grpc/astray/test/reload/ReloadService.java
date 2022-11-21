package com.github.charlemaznable.grpc.astray.test.reload;

import com.github.charlemaznable.grpc.astray.server.GRpcMethod;
import com.github.charlemaznable.grpc.astray.server.GRpcService;

@GRpcService("Reload")
public class ReloadService {

    @GRpcMethod("Test")
    public String test(String req) {
        return "response: " + req + ", ^_^";
    }
}
