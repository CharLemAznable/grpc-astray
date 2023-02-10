package com.github.charlemaznable.grpc.astray.test.common;

import com.github.charlemaznable.grpc.astray.client.configurer.configservice.GRpcCommonConfig;
import com.github.charlemaznable.grpc.astray.client.configurer.inprocess.InProcessChannelBuilderConfig;
import lombok.AllArgsConstructor;

@AllArgsConstructor
public class EmbeddedChannelConfigurer implements GRpcCommonConfig, InProcessChannelBuilderConfig {

    private String name;

    @Override
    public String targetsString() {
        return name;
    }

    @Override
    public String channelBalancerString() {
        return null;
    }
}
