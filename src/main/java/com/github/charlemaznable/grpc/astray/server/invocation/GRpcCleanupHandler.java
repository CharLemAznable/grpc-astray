package com.github.charlemaznable.grpc.astray.server.invocation;

import java.lang.annotation.Documented;
import java.lang.annotation.ElementType;
import java.lang.annotation.Inherited;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * Marks the annotated method eligible for handling the server call closed.
 *
 * The signature of the method HAS to be as: <br>
 * {@code void handlerName()}
 */
@Documented
@Inherited
@Target({ElementType.METHOD, ElementType.ANNOTATION_TYPE})
@Retention(RetentionPolicy.RUNTIME)
public @interface GRpcCleanupHandler {
}
