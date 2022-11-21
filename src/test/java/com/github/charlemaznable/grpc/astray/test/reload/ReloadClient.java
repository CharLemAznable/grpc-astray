package com.github.charlemaznable.grpc.astray.test.reload;

import com.github.charlemaznable.core.lang.Reloadable;
import com.github.charlemaznable.grpc.astray.client.GRpcChannel;
import com.github.charlemaznable.grpc.astray.client.GRpcClient;

@GRpcClient("Reload")
@GRpcChannel(channelProvider = ReloadChannelProvider.class)
public interface ReloadClient extends Reloadable {

    String test(String req);
}
