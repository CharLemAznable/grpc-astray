package com.github.charlemaznable.grpc.astray.client.configurer.configservice;

import com.github.charlemaznable.configservice.Config;
import com.github.charlemaznable.grpc.astray.client.GRpcChannelBalance;
import com.github.charlemaznable.grpc.astray.client.configurer.GRpcChannelBalanceConfigurer;

import java.util.Optional;

import static com.github.charlemaznable.core.lang.Condition.notNullThen;

public interface GRpcChannelBalanceConfig extends GRpcChannelBalanceConfigurer {

    @Config("channelBalancer")
    String channelBalancerString();

    @Override
    default GRpcChannelBalance.ChannelBalancer channelBalancer() {
        return notNullThen(channelBalancerString(), v -> Optional
                .ofNullable(GRpcChannelBalance.BalanceType.resolve(v))
                .map(GRpcChannelBalance.BalanceType::getChannelBalancer).orElse(null));
    }
}
