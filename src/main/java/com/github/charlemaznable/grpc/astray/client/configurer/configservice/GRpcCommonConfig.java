package com.github.charlemaznable.grpc.astray.client.configurer.configservice;

import com.github.charlemaznable.grpc.astray.client.configurer.GRpcInitializationConfigurer;

public interface GRpcCommonConfig extends GRpcInitializationConfigurer,
        GRpcChannelConfig, GRpcChannelBalanceConfig {
}
