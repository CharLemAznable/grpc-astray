package com.github.charlemaznable.grpc.astray.server.autoconfigure;

import org.springframework.context.ApplicationContext;
import org.springframework.context.event.ApplicationContextEvent;

import java.io.Serial;

public final class GRpcServerInitializedEvent extends ApplicationContextEvent {

    @Serial
    private static final long serialVersionUID = -9044518073740741490L;

    public GRpcServerInitializedEvent(ApplicationContext context) {
        super(context);
    }
}
