package com.github.charlemaznable.grpc.astray.server.validation;

import com.github.charlemaznable.grpc.astray.server.common.GRpcRuntimeExceptionAdapter;
import com.github.charlemaznable.grpc.astray.server.common.MessageBlockingServerCallListener;
import com.github.charlemaznable.grpc.astray.server.invocation.handle.GRpcHandlingSupport;
import com.github.charlemaznable.grpc.astray.server.validation.group.RequestMessage;
import com.github.charlemaznable.grpc.astray.server.validation.group.ResponseMessage;
import io.grpc.ForwardingServerCall;
import io.grpc.Metadata;
import io.grpc.ServerCall;
import io.grpc.ServerCallHandler;
import io.grpc.ServerInterceptor;
import io.grpc.Status;
import lombok.Getter;
import lombok.val;
import org.springframework.core.Ordered;

import javax.validation.ConstraintViolationException;
import javax.validation.Validator;
import java.util.Optional;

import static com.github.charlemaznable.core.lang.Condition.checkEmptyRun;

final class GRpcValidatingInterceptor implements ServerInterceptor, Ordered {

    private final Validator validator;
    private final GRpcHandlingSupport handlingSupport;
    @Getter
    private final int order;

    public GRpcValidatingInterceptor(Validator validator,
                                     GRpcHandlingSupport handlingSupport,
                                     GRpcValidationProperties validationProperties) {
        this.validator = validator;
        this.handlingSupport = handlingSupport;
        this.order = Optional.ofNullable(validationProperties.getInterceptorOrder())
                .orElse(Ordered.HIGHEST_PRECEDENCE + 10);
    }

    @Override
    public <Q, R> ServerCall.Listener<Q> interceptCall(ServerCall<Q, R> call, Metadata headers,
                                                       ServerCallHandler<Q, R> next) {
        val validationServerCall = new ForwardingServerCall.SimpleForwardingServerCall<Q, R>(call) {

            @Override
            public void sendMessage(R message) {
                checkEmptyRun(validator.validate(message, ResponseMessage.class),
                        () -> super.sendMessage(message),
                        violations -> handlingSupport.handleException(new GRpcRuntimeExceptionAdapter(
                                        new ConstraintViolationException(violations), Status.FAILED_PRECONDITION),
                                this, headers, b -> b.response(message)));
            }
        };

        ServerCall.Listener<Q> listener = next.startCall(validationServerCall, headers);

        return new MessageBlockingServerCallListener<Q>(listener) {

            @Override
            public void onMessage(Q message) {
                checkEmptyRun(validator.validate(message, RequestMessage.class),
                        () -> super.onMessage(message),
                        violations -> {
                            blockMessage();
                            handlingSupport.handleException(new GRpcRuntimeExceptionAdapter(
                                            new ConstraintViolationException(violations), Status.INVALID_ARGUMENT),
                                    validationServerCall, headers, b -> b.request(message));
                        });
            }
        };
    }
}
