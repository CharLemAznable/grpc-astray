package com.github.charlemaznable.grpc.astray.test.interceptor;

import com.github.charlemaznable.grpc.astray.server.common.MessageBlockingServerCallListener;
import io.grpc.ForwardingServerCall;
import io.grpc.Metadata;
import io.grpc.ServerCall;
import io.grpc.ServerCallHandler;
import io.grpc.ServerInterceptor;
import lombok.val;
import org.springframework.core.annotation.Order;

@SuppressWarnings("unchecked")
@Order(0)
public class InterceptorChanger implements ServerInterceptor {

    @Override
    public <Q, R> ServerCall.Listener<Q> interceptCall(ServerCall<Q, R> call, Metadata headers,
                                                       ServerCallHandler<Q, R> next) {
        val validationServerCall = new ForwardingServerCall.SimpleForwardingServerCall<Q, R>(call) {

            @Override
            public void sendMessage(R message) {
                super.sendMessage((R) ("intercepted " + message));
            }
        };

        ServerCall.Listener<Q> listener = next.startCall(validationServerCall, headers);

        return new MessageBlockingServerCallListener<Q>(listener) {

            @Override
            public void onMessage(Q message) {
                super.onMessage((Q) ("intercepted " + message));
            }
        };
    }
}
