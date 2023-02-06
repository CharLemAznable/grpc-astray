package com.github.charlemaznable.grpc.astray.client.configurer;

import io.grpc.Channel;

import java.util.function.Function;

public interface GRpcChannelBuilderConfigurer extends GRpcConfigurer {

    Function<String, Channel> channelBuilder();
}
