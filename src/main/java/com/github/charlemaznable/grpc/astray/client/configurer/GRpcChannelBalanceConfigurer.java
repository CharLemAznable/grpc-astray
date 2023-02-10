package com.github.charlemaznable.grpc.astray.client.configurer;

import com.github.charlemaznable.grpc.astray.client.GRpcChannelBalance;

public interface GRpcChannelBalanceConfigurer extends GRpcConfigurer {

    GRpcChannelBalance.ChannelBalancer channelBalancer();
}
