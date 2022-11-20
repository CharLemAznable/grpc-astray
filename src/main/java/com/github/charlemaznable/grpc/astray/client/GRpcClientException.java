package com.github.charlemaznable.grpc.astray.client;

public final class GRpcClientException extends RuntimeException {

    private static final long serialVersionUID = 1277977336905736756L;

    public GRpcClientException(String msg) {
        super(msg);
    }
}
