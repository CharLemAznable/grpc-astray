package com.github.charlemaznable.grpc.astray.server.invocation.exception;

import org.springframework.context.annotation.Conditional;

import java.lang.annotation.Documented;
import java.lang.annotation.ElementType;
import java.lang.annotation.Inherited;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

@Documented
@Inherited
@Target({ElementType.TYPE, ElementType.METHOD})
@Retention(RetentionPolicy.RUNTIME)
@Conditional(OnMissingExceptionHandlerCondition.class)
public @interface ConditionalOnMissingExceptionHandler {

    Class<? extends Throwable> value();
}