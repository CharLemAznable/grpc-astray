package com.github.charlemaznable.grpc.astray.server.invocation.exception;

import io.grpc.Attributes;
import io.grpc.Metadata;
import io.grpc.MethodDescriptor;
import lombok.Builder;
import lombok.Getter;
import lombok.Setter;

import java.util.Optional;

@Builder
@Getter
public final class GRpcExceptionScope {

    private MethodDescriptor<?, ?> methodDescriptor;
    private Attributes methodCallAttributes;
    private Metadata callHeaders;
    private Object request;
    private Object response;
    private Object hint;
    @Builder.Default
    private final Metadata trailers = new Metadata();
    @Builder.Default
    private Recovery recovery = new Recovery();

    public Object getRequestOrResponse(){
        return Optional.ofNullable(request).orElse(response);
    }

    public <T> Optional<T> getHintAs(Class<T> clazz){
        return Optional.ofNullable(hint)
                .filter(clazz::isInstance)
                .map(clazz::cast);
    }

    @Getter
    public static final class Recovery {

        private final Metadata headers = new Metadata();
        @Setter
        private Object response;
    }
}
