package com.github.charlemaznable.grpc.astray.server.invocation;

import java.lang.annotation.Documented;
import java.lang.annotation.ElementType;
import java.lang.annotation.Inherited;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * Marks the annotated method eligible for handling the specific exception type.
 *
 * The signature of the method HAS to be as: <br>
 * {@code io.grpc.Status handlerName(Exception exception, GRpcExceptionScope scope)}
 */
@Documented
@Inherited
@Target({ElementType.METHOD, ElementType.ANNOTATION_TYPE})
@Retention(RetentionPolicy.RUNTIME)
public @interface GRpcExceptionHandler {
}
