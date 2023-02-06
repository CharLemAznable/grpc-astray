package com.github.charlemaznable.grpc.astray.test.reload;

import com.github.charlemaznable.configservice.Config;
import com.github.charlemaznable.grpc.astray.client.configurer.GRpcChannelBuilderConfigurer;
import com.github.charlemaznable.grpc.astray.client.configurer.GRpcCommonConfig;
import io.grpc.Channel;
import io.grpc.inprocess.InProcessChannelBuilder;

import java.util.function.Function;

@Config(keyset = "GRPC", key = "reload")
public interface ReloadChannelConfig extends GRpcCommonConfig, GRpcChannelBuilderConfigurer {

    @Override
    default Function<String, Channel> channelBuilder() {
        return s -> InProcessChannelBuilder.forName(s)
                .directExecutor().usePlaintext().build();
    }
}
