package com.github.charlemaznable.grpc.astray.server.invocation;

import com.github.charlemaznable.grpc.astray.server.GRpcGlobalInterceptor;
import com.github.charlemaznable.grpc.astray.server.autoconfigure.GRpcServerProperties;
import com.github.charlemaznable.grpc.astray.server.autoconfigure.GRpcServicesRegistry;
import com.github.charlemaznable.grpc.astray.server.invocation.handle.GRpcHandlingSupport;
import org.springframework.context.ApplicationContext;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class GRpcInvocationConfiguration {

    @Bean
    public GRpcHandlingSupport invocationHandlingSupport
            (ApplicationContext applicationContext, GRpcServicesRegistry gRpcServicesRegistry) {
        return new GRpcHandlingSupport(applicationContext, gRpcServicesRegistry);
    }

    @Bean
    @GRpcGlobalInterceptor
    public GRpcInvocationHandlerInterceptor invocationInterceptor(
            GRpcHandlingSupport invocationHandlingSupport,
            GRpcServerProperties serverProperties) {
        return new GRpcInvocationHandlerInterceptor(
                invocationHandlingSupport, serverProperties);
    }
}
