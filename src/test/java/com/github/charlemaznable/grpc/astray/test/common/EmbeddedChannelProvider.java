package com.github.charlemaznable.grpc.astray.test.common;

import com.github.charlemaznable.grpc.astray.client.GRpcChannel.ChannelProvider;
import io.grpc.Channel;
import io.grpc.inprocess.InProcessChannelBuilder;
import lombok.AllArgsConstructor;

import java.util.List;

import static com.github.charlemaznable.core.lang.Listt.newArrayList;

@AllArgsConstructor
public class EmbeddedChannelProvider implements ChannelProvider {

    private String name;

    @Override
    public List<Channel> channels(Class<?> clazz) {
        return newArrayList(InProcessChannelBuilder.forName(name)
                .directExecutor().usePlaintext().build());
    }
}
