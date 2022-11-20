package com.github.charlemaznable.grpc.astray.server.autoconfigure;

import io.grpc.ServerBuilder;

public interface GRpcServerBuilderConfigurer {

    void configure(ServerBuilder<?> serverBuilder);
}
