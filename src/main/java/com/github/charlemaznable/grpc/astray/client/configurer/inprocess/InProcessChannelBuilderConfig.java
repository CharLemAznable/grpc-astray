package com.github.charlemaznable.grpc.astray.client.configurer.inprocess;

import com.github.charlemaznable.grpc.astray.client.configurer.GRpcChannelBuilderConfigurer;
import io.grpc.Channel;
import io.grpc.inprocess.InProcessChannelBuilder;

import java.util.function.Function;

public interface InProcessChannelBuilderConfig extends GRpcChannelBuilderConfigurer {

    @Override
    default Function<String, Channel> channelBuilder() {
        return s -> InProcessChannelBuilder.forName(s)
                .directExecutor().usePlaintext().build();
    }
}
