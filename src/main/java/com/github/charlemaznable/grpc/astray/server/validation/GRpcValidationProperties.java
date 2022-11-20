package com.github.charlemaznable.grpc.astray.server.validation;

import lombok.Getter;
import lombok.Setter;
import org.springframework.boot.context.properties.ConfigurationProperties;

@ConfigurationProperties("grpc.validation")
@Getter
@Setter
public class GRpcValidationProperties {

    private Integer interceptorOrder;
}
