package com.github.charlemaznable.grpc.astray.test.reload;

import com.github.charlemaznable.grpc.astray.client.GRpcChannel.ChannelProvider;
import io.grpc.Channel;
import io.grpc.inprocess.InProcessChannelBuilder;
import lombok.Setter;
import org.springframework.stereotype.Component;

import java.util.List;
import java.util.stream.Collectors;

import static com.github.charlemaznable.core.lang.Listt.newArrayList;

@Component
@Setter
public class ReloadChannelProvider implements ChannelProvider {

    private List<String> names = newArrayList("none");

    @Override
    public List<Channel> channels(Class<?> clazz) {
        return names.stream().map(name -> InProcessChannelBuilder.forName(name)
                .directExecutor().usePlaintext().build()).collect(Collectors.toList());
    }
}
