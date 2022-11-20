package com.github.charlemaznable.grpc.astray.server.invocation.handle;

import com.github.charlemaznable.grpc.astray.server.invocation.exception.GRpcExceptionScope;
import io.grpc.Status;
import lombok.AccessLevel;
import lombok.AllArgsConstructor;
import lombok.Getter;

import java.lang.reflect.Method;
import java.util.Optional;

import static com.github.charlemaznable.grpc.astray.server.invocation.handle.GRpcHandlerMethodElf.parseHandledException;
import static org.springframework.util.ReflectionUtils.invokeMethod;
import static org.springframework.util.ReflectionUtils.makeAccessible;

@AllArgsConstructor(access = AccessLevel.PRIVATE)
@Getter
final class GRpcExceptionHandlerMethod {

    private final Method method;
    private final Object target;
    private final Class<? extends Throwable> exceptionType;

    Status invoke(Throwable e, GRpcExceptionScope scope) {
        makeAccessible(method);
        return (Status) invokeMethod(method, target, e, scope);
    }

    static Optional<GRpcExceptionHandlerMethod> create(Object target, Method method) {
        return parseHandledException(method).map(matchExceptionType ->
                new GRpcExceptionHandlerMethod(method, target, matchExceptionType));
    }
}
