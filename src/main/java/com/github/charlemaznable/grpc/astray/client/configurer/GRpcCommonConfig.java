package com.github.charlemaznable.grpc.astray.client.configurer;

import com.github.charlemaznable.configservice.Config;
import com.github.charlemaznable.grpc.astray.client.GRpcChannelBalance;
import com.google.common.base.Splitter;

import java.util.List;
import java.util.Optional;

import static com.github.charlemaznable.core.lang.Condition.notNullThen;

public interface GRpcCommonConfig extends GRpcChannelConfigurer, GRpcChannelBalanceConfigurer {

    @Config("targets")
    String targetsString();

    @Config("channelBalancer")
    String channelBalancerString();

    @Override
    default List<String> targets() {
        return notNullThen(targetsString(), v -> Splitter.on(",")
                .omitEmptyStrings().trimResults().splitToList(v));
    }

    @Override
    default GRpcChannelBalance.ChannelBalancer channelBalancer() {
        return notNullThen(channelBalancerString(), v -> Optional
                .ofNullable(GRpcChannelBalance.BalanceType.resolve(v))
                .map(GRpcChannelBalance.BalanceType::getChannelBalancer).orElse(null));
    }
}
