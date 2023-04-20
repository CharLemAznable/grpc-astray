package com.github.charlemaznable.grpc.astray.client.elf;

import io.grpc.Channel;

public interface GlobalChannelProcessor {

    Channel process(Channel channel);
}
