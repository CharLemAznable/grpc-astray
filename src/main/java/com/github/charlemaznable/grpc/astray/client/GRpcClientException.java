package com.github.charlemaznable.grpc.astray.client;

import java.io.Serial;

public final class GRpcClientException extends RuntimeException {

    @Serial
    private static final long serialVersionUID = 1277977336905736756L;

    public GRpcClientException(String msg) {
        super(msg);
    }
}
