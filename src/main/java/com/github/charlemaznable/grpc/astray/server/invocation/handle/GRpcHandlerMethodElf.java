package com.github.charlemaznable.grpc.astray.server.invocation.handle;

import com.github.charlemaznable.grpc.astray.server.invocation.exception.GRpcExceptionScope;
import io.grpc.Status;
import lombok.NoArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import lombok.val;
import org.springframework.util.Assert;

import java.lang.reflect.Method;
import java.util.Optional;

import static lombok.AccessLevel.PRIVATE;

@SuppressWarnings("unchecked")
@Slf4j(topic = "grpc.invocation.handler.resolver")
@NoArgsConstructor(access = PRIVATE)
public final class GRpcHandlerMethodElf {

    public static Optional<Class<?>> parseHandledFilterType(Method method) {
        try {
            Assert.state(1 == method.getParameterCount(), () ->
                    "There should be exactly 1 parameters on method " + method);
            val parameterType = method.getParameterTypes()[0];
            val returnType = method.getReturnType();
            Assert.state(returnType.isAssignableFrom(parameterType), () ->
                    returnTypeMessage(method, "assignable from " + parameterType.getName()));
            return Optional.of(parameterType);
        } catch (IllegalArgumentException e) {
            log.warn("Resolving invocation handler method error: ", e);
            return Optional.empty();
        }
    }

    public static Optional<Object> parseHandledCleanup(Method method) {
        try {
            Assert.state(0 == method.getParameterCount(), () ->
                    "There should be none parameter on method " + method);
            val returnType = method.getReturnType();
            Assert.state(void.class == returnType || Void.class == returnType, () ->
                    returnTypeMessage(method, "void"));
            return Optional.of(new Object());
        } catch (IllegalArgumentException e) {
            log.warn("Resolving cleanup handler method error: ", e);
            return Optional.empty();
        }
    }

    public static Optional<Class<? extends Throwable>> parseHandledException(Method method) {
        try {
            Assert.state(2 == method.getParameterCount(), () ->
                    "There should be exactly 2 parameters on method " + method);
            val parameterTypes = method.getParameterTypes();
            Assert.state(Throwable.class.isAssignableFrom(parameterTypes[0]), () ->
                    "First parameter of method " + method + " has to be of type" + Throwable.class.getName());
            Assert.state(GRpcExceptionScope.class.isAssignableFrom(parameterTypes[1]), () ->
                    "Second parameter of method " + method + " has to be of type" + GRpcExceptionScope.class.getName());
            Assert.state(Status.class.isAssignableFrom(method.getReturnType()), () ->
                    returnTypeMessage(method, Status.class.getName()));
            return Optional.of((Class<? extends Throwable>) parameterTypes[0]);
        } catch (IllegalArgumentException e) {
            log.warn("Resolving exception handler method error: ", e);
            return Optional.empty();
        }
    }

    private static String returnTypeMessage(Method method, String returnTypeName) {
        return "Return type of method " + method + " has to be " + returnTypeName;
    }
}
