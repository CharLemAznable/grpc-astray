package com.github.charlemaznable.grpc.astray.test.validation;

import com.github.charlemaznable.grpc.astray.client.GRpcClient;
import com.github.charlemaznable.grpc.astray.test.common.EmbeddedChannel;

@GRpcClient("Validation")
@EmbeddedChannel
public interface ValidationClient {

    ValidationResponse call(ValidationRequest req);
}
