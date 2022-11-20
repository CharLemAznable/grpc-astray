package com.github.charlemaznable.grpc.astray.server.invocation.handle;

import lombok.AccessLevel;
import lombok.AllArgsConstructor;
import lombok.Getter;

import java.lang.reflect.Method;
import java.util.Optional;

import static com.github.charlemaznable.grpc.astray.server.invocation.handle.GRpcHandlerMethodElf.parseHandledFilterType;
import static org.springframework.util.ReflectionUtils.invokeMethod;
import static org.springframework.util.ReflectionUtils.makeAccessible;

@AllArgsConstructor(access = AccessLevel.PRIVATE)
@Getter
final class GRpcResponseHandlerMethod {

    private final Method method;
    private final Object target;
    private final Class<?> responseType;

    Object invoke(Object request) {
        makeAccessible(method);
        return invokeMethod(method, target, request);
    }

    static Optional<GRpcResponseHandlerMethod> create(Object target, Method method) {
        return parseHandledFilterType(method).map(matchResponseType ->
                new GRpcResponseHandlerMethod(method, target, matchResponseType));
    }
}
