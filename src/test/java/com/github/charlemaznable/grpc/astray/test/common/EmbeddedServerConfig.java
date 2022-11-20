package com.github.charlemaznable.grpc.astray.test.common;

import com.github.charlemaznable.grpc.astray.client.GRpcChannel.ChannelProvider;
import io.grpc.ServerBuilder;
import io.grpc.inprocess.InProcessServerBuilder;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
@ConditionalOnProperty(prefix = "grpc", name = "embeddedServerName")
@EnableConfigurationProperties(EmbeddedServerProperties.class)
public class EmbeddedServerConfig {

    @Bean
    public ServerBuilder embeddedServerBuilder(EmbeddedServerProperties embeddedServerProperties) {
        return InProcessServerBuilder.forName(embeddedServerProperties.getEmbeddedServerName());
    }

    @Bean
    public ChannelProvider embeddedChannelProvider(EmbeddedServerProperties embeddedServerProperties) {
        return new EmbeddedChannelProvider(embeddedServerProperties.getEmbeddedServerName());
    }
}
