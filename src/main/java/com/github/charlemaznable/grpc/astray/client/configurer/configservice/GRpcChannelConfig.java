package com.github.charlemaznable.grpc.astray.client.configurer.configservice;

import com.github.charlemaznable.configservice.Config;
import com.github.charlemaznable.grpc.astray.client.configurer.GRpcChannelConfigurer;
import com.google.common.base.Splitter;

import java.util.List;

import static com.github.charlemaznable.core.lang.Condition.notNullThen;

public interface GRpcChannelConfig extends GRpcChannelConfigurer {

    @Config("targets")
    String targetsString();

    @Override
    default List<String> targets() {
        return notNullThen(targetsString(), v -> Splitter.on(",")
                .omitEmptyStrings().trimResults().splitToList(v));
    }
}
