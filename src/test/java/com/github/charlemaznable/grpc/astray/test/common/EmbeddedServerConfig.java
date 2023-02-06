package com.github.charlemaznable.grpc.astray.test.common;

import io.grpc.ServerBuilder;
import io.grpc.inprocess.InProcessServerBuilder;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@SuppressWarnings("rawtypes")
@Configuration
@ConditionalOnProperty(prefix = "grpc", name = "embeddedServerName")
public class EmbeddedServerConfig {

    @Value("${grpc.embeddedServerName}")
    private String embeddedServerName;

    @Bean
    public ServerBuilder embeddedServerBuilder() {
        return InProcessServerBuilder.forName(embeddedServerName);
    }

    @Bean
    public EmbeddedChannelConfigurer embeddedChannelConfigurer() {
        return new EmbeddedChannelConfigurer(embeddedServerName);
    }
}
