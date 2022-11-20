package com.github.charlemaznable.grpc.astray.server.invocation;

import com.github.charlemaznable.grpc.astray.server.autoconfigure.GRpcServerProperties;
import com.github.charlemaznable.grpc.astray.server.common.MessageBlockingServerCallListener;
import com.github.charlemaznable.grpc.astray.server.invocation.handle.GRpcHandlingSupport;
import io.grpc.ForwardingServerCall;
import io.grpc.Metadata;
import io.grpc.ServerCall;
import io.grpc.ServerCallHandler;
import io.grpc.ServerInterceptor;
import io.grpc.Status;
import lombok.Getter;
import lombok.val;
import org.springframework.core.Ordered;

import java.util.Optional;
import java.util.concurrent.atomic.AtomicBoolean;

final class GRpcInvocationHandlerInterceptor implements ServerInterceptor, Ordered {

    private final GRpcHandlingSupport handlingSupport;
    @Getter
    private final int order;

    public GRpcInvocationHandlerInterceptor(GRpcHandlingSupport handlingSupport,
                                            GRpcServerProperties serverProperties) {
        this.handlingSupport = handlingSupport;
        this.order = Optional.ofNullable(serverProperties.getInvocation())
                .map(GRpcServerProperties.InvocationProperties::getInterceptorOrder)
                .orElse(Ordered.HIGHEST_PRECEDENCE);
    }

    @Override
    public <Q, R> ServerCall.Listener<Q> interceptCall(ServerCall<Q, R> call, Metadata headers,
                                                       ServerCallHandler<Q, R> next) {
        val callIsClosed = new AtomicBoolean(false);
        val serverCall = new ForwardingServerCall.SimpleForwardingServerCall<Q, R>(call) {

            private R response;

            @Override
            public void sendMessage(R message) {
                try {
                    response = handlingSupport
                            .handleResponse(this, message);
                    super.sendMessage(response);
                } catch (RuntimeException e) {
                    handlingSupport.handleException(
                            e, this, headers, b -> b.response(response));
                }
            }

            @Override
            public void close(Status status, Metadata trailers) {
                if (callIsClosed.compareAndSet(false, true)) {
                    super.close(status, trailers);
                    handlingSupport.handleCleanup(this);
                }
            }
        };

        final ServerCall.Listener<Q> listener;
        try {
            listener = next.startCall(serverCall, headers);
        } catch (RuntimeException e) {
            handlingSupport.handleException(e, serverCall, headers);
            return new ServerCall.Listener<Q>() {};
        }

        return new MessageBlockingServerCallListener<Q>(listener) {

            private Q request;

            @Override
            public void onMessage(Q message) {
                try {
                    request = handlingSupport
                            .handleRequest(serverCall, message);
                    super.onMessage(request);
                } catch (RuntimeException e) {
                    blockMessage();
                    handlingSupport.handleException(
                            e, serverCall, headers, b -> b.request(request));
                }
            }

            @Override
            public void onHalfClose() {
                try {
                    if (!callIsClosed.get()) super.onHalfClose();
                } catch (RuntimeException e) {
                    handlingSupport.handleException(
                            e, serverCall, headers, b -> b.request(request));
                }
            }
        };
    }
}
