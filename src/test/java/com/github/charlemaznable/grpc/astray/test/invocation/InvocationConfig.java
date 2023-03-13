package com.github.charlemaznable.grpc.astray.test.invocation;

import com.github.charlemaznable.grpc.astray.client.GRpcScannerRegistrar;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.ComponentScan;

@ComponentScan
public class InvocationConfig {

    @Bean
    public GRpcScannerRegistrar.GRpcClientFactoryBean invocationClient() {
        return GRpcScannerRegistrar.buildFactoryBean(InvocationClient.class);
    }
}
