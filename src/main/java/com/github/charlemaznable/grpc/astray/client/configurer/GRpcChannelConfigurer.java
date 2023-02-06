package com.github.charlemaznable.grpc.astray.client.configurer;

import java.util.List;

public interface GRpcChannelConfigurer extends GRpcConfigurer {

    List<String> targets();
}
