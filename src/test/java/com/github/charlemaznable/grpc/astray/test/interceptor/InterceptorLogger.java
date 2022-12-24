package com.github.charlemaznable.grpc.astray.test.interceptor;

import com.github.charlemaznable.grpc.astray.server.common.MessageBlockingServerCallListener;
import io.grpc.ForwardingServerCall;
import io.grpc.Metadata;
import io.grpc.ServerCall;
import io.grpc.ServerCallHandler;
import io.grpc.ServerInterceptor;
import lombok.Getter;
import lombok.val;
import org.springframework.core.annotation.Order;
import org.springframework.stereotype.Component;

@Component
@Getter
@Order(1)
public class InterceptorLogger implements ServerInterceptor {

    private String logRequest;
    private String logResponse;

    @Override
    public <Q, R> ServerCall.Listener<Q> interceptCall(ServerCall<Q, R> call, Metadata headers,
                                                       ServerCallHandler<Q, R> next) {
        val validationServerCall = new ForwardingServerCall.SimpleForwardingServerCall<>(call) {

            @Override
            public void sendMessage(R message) {
                logResponse = message.toString();
                super.sendMessage(message);
            }
        };

        ServerCall.Listener<Q> listener = next.startCall(validationServerCall, headers);

        return new MessageBlockingServerCallListener<>(listener) {

            @Override
            public void onMessage(Q message) {
                logRequest = message.toString();
                super.onMessage(message);
            }
        };
    }
}
