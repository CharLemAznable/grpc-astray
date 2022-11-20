package com.github.charlemaznable.grpc.astray.test.common;

import lombok.Getter;
import lombok.Setter;
import org.springframework.boot.context.properties.ConfigurationProperties;

@ConfigurationProperties("grpc")
@Getter
@Setter
public class EmbeddedServerProperties {

    private String embeddedServerName;
}
