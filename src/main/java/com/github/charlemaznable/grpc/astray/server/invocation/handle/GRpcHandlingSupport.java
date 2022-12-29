package com.github.charlemaznable.grpc.astray.server.invocation.handle;

import com.github.charlemaznable.grpc.astray.server.autoconfigure.GRpcServicesRegistry;
import com.github.charlemaznable.grpc.astray.server.common.GRpcRuntimeExceptionAdapter;
import com.github.charlemaznable.grpc.astray.server.invocation.exception.GRpcExceptionScope;
import io.grpc.Metadata;
import io.grpc.ServerCall;
import io.grpc.Status;
import lombok.extern.slf4j.Slf4j;
import lombok.val;
import org.springframework.context.ApplicationContext;

import java.util.Optional;
import java.util.function.Consumer;

import static java.util.Objects.nonNull;

@SuppressWarnings("unchecked")
@Slf4j(topic = "grpc.invocation.handling")
public final class GRpcHandlingSupport {

    private final GRpcHandlerMethodResolver methodResolver;

    public GRpcHandlingSupport(ApplicationContext applicationContext,
                               GRpcServicesRegistry gRpcServicesRegistry) {
        methodResolver = new GRpcHandlerMethodResolver(applicationContext, gRpcServicesRegistry);
    }

    public <Q, R> Q handleRequest(ServerCall<Q, R> call, Q request) {
        Q requestToSend = request;
        val handlerMethods = methodResolver.resolveMethodsByRequest(
                call.getMethodDescriptor().getServiceName(), request);
        for (val handlerMethod : handlerMethods) {
            requestToSend = (Q) handlerMethod.invoke(requestToSend);
        }
        return requestToSend;
    }

    public <Q, R> R handleResponse(ServerCall<Q, R> call, R response) {
        R responseToSend = response;
        val handlerMethods = methodResolver.resolveMethodsByResponse(
                call.getMethodDescriptor().getServiceName(), response);
        for (val handlerMethod : handlerMethods) {
            responseToSend = (R) handlerMethod.invoke(responseToSend);
        }
        return responseToSend;
    }

    public <Q, R> void handleException(RuntimeException e, ServerCall<Q, R> call, Metadata headers) {
        handleException(e, call, headers, null);
    }

    public <Q, R> void handleException(RuntimeException e, ServerCall<Q, R> call, Metadata headers,
                                       Consumer<GRpcExceptionScope.GRpcExceptionScopeBuilder> customizer) {
        Status statusToSend = Status.INTERNAL;
        Metadata trailersToSend = null;
        R responseToSend = null;
        Metadata responseHeadersToSend = null;
        val handlerMethod = methodResolver.resolveMethodByThrowable(
                call.getMethodDescriptor().getServiceName(), e);
        if (handlerMethod.isPresent()) {
            val exceptionScopeBuilder = GRpcExceptionScope.builder()
                    .callHeaders(headers)
                    .methodCallAttributes(call.getAttributes())
                    .methodDescriptor(call.getMethodDescriptor())
                    .hint(GRpcRuntimeExceptionAdapter.getHint(e));
            Optional.ofNullable(customizer)
                    .ifPresent(c -> c.accept(exceptionScopeBuilder));
            val excScope = exceptionScopeBuilder.build();
            val handler = handlerMethod.get();
            try {
                statusToSend = handler.getLeft().invoke(handler.getRight(), excScope);
                trailersToSend = excScope.getTrailers();
                responseToSend = (R) excScope.getRecovery().getResponse();
                responseHeadersToSend = excScope.getRecovery().getHeaders();
            } catch (Exception handlerException) {
                log.error("Caught exception while executing handler method {}, returning {} status.",
                        handler.getLeft().getMethod(), statusToSend, handlerException);
            }
        }
        if (nonNull(responseToSend)) {
            log.warn("Recoverying call with {}", responseToSend);
            call.sendHeaders(Optional.ofNullable(responseHeadersToSend).orElseGet(Metadata::new));
            call.sendMessage(responseToSend);
        }
        log.warn("Closing call with {}", statusToSend, GRpcRuntimeExceptionAdapter.unwrap(e));
        call.close(statusToSend, Optional.ofNullable(trailersToSend).orElseGet(Metadata::new));
    }

    public <Q, R> void handleCleanup(ServerCall<Q, R> call) {
        val handlerMethods = methodResolver.resolveCleanupMethods(
                call.getMethodDescriptor().getServiceName());
        for (val handlerMethod : handlerMethods) {
            handlerMethod.invoke();
        }
    }
}
