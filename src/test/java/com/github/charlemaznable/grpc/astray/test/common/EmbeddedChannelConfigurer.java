package com.github.charlemaznable.grpc.astray.test.common;

import com.github.charlemaznable.grpc.astray.client.configurer.GRpcChannelBuilderConfigurer;
import com.github.charlemaznable.grpc.astray.client.configurer.GRpcCommonConfig;
import io.grpc.Channel;
import io.grpc.inprocess.InProcessChannelBuilder;
import lombok.AllArgsConstructor;

import java.util.function.Function;

@AllArgsConstructor
public class EmbeddedChannelConfigurer implements GRpcCommonConfig, GRpcChannelBuilderConfigurer {

    private String name;

    @Override
    public String targetsString() {
        return name;
    }

    @Override
    public String channelBalancerString() {
        return null;
    }

    @Override
    public Function<String, Channel> channelBuilder() {
        return s -> InProcessChannelBuilder.forName(s)
                .directExecutor().usePlaintext().build();
    }
}
