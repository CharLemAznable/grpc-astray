package com.github.charlemaznable.grpc.astray.test.reload;

import com.github.charlemaznable.core.lang.Reloadable;
import com.github.charlemaznable.grpc.astray.client.GRpcClient;
import com.github.charlemaznable.grpc.astray.client.GRpcConfigurerWith;

@GRpcClient("Reload")
@GRpcConfigurerWith(ReloadChannelConfig.class)
public interface ReloadClient extends Reloadable {

    String test(String req);
}
