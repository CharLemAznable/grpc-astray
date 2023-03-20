package com.github.charlemaznable.grpc.astray.test.common;

import com.github.charlemaznable.grpc.astray.client.configurer.GRpcInitializationContext;
import com.github.charlemaznable.grpc.astray.client.configurer.configservice.GRpcCommonConfig;
import com.github.charlemaznable.grpc.astray.client.configurer.inprocess.InProcessChannelBuilderConfig;
import com.github.charlemaznable.grpc.astray.test.interceptor.InterceptorClient;
import com.github.charlemaznable.grpc.astray.test.invocation.InvocationClient;
import com.github.charlemaznable.grpc.astray.test.validation.ValidationClient;
import lombok.AllArgsConstructor;

import static org.junit.jupiter.api.Assertions.assertEquals;

@AllArgsConstructor
public class EmbeddedChannelConfigurer implements GRpcCommonConfig, InProcessChannelBuilderConfig {

    private String name;

    @Override
    public String targetsString() {
        if ("interceptor".equals(name)) {
            assertEquals(InterceptorClient.class, GRpcInitializationContext.getGRpcClass());
        } else if ("invocation".equals(name)) {
            assertEquals(InvocationClient.class, GRpcInitializationContext.getGRpcClass());
        } else if ("validation".equals(name)) {
            assertEquals(ValidationClient.class, GRpcInitializationContext.getGRpcClass());
        }
        return name;
    }

    @Override
    public String channelBalancerString() {
        return null;
    }
}
