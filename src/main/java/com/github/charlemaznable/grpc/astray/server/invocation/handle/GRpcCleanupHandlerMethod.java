package com.github.charlemaznable.grpc.astray.server.invocation.handle;

import lombok.AccessLevel;
import lombok.AllArgsConstructor;
import lombok.Getter;

import java.lang.reflect.Method;
import java.util.Optional;

import static com.github.charlemaznable.grpc.astray.server.invocation.handle.GRpcHandlerMethodElf.parseHandledCleanup;
import static org.springframework.util.ReflectionUtils.invokeMethod;
import static org.springframework.util.ReflectionUtils.makeAccessible;

@AllArgsConstructor(access = AccessLevel.PRIVATE)
@Getter
final class GRpcCleanupHandlerMethod {

    private final Method method;
    private final Object target;

    void invoke() {
        makeAccessible(method);
        invokeMethod(method, target);
    }

    static Optional<GRpcCleanupHandlerMethod> create(Object target, Method method) {
        return parseHandledCleanup(method).map(match ->
                new GRpcCleanupHandlerMethod(method, target));
    }
}
