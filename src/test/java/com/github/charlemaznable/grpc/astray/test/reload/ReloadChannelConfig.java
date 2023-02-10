package com.github.charlemaznable.grpc.astray.test.reload;

import com.github.charlemaznable.configservice.Config;
import com.github.charlemaznable.grpc.astray.client.configurer.configservice.GRpcCommonConfig;
import com.github.charlemaznable.grpc.astray.client.configurer.inprocess.InProcessChannelBuilderConfig;

@Config(keyset = "GRPC", key = "reload")
public interface ReloadChannelConfig extends GRpcCommonConfig, InProcessChannelBuilderConfig {
}
