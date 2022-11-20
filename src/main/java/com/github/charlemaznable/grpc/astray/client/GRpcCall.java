package com.github.charlemaznable.grpc.astray.client;

import org.springframework.core.annotation.AliasFor;

import java.lang.annotation.Documented;
import java.lang.annotation.ElementType;
import java.lang.annotation.Inherited;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

@Documented
@Inherited
@Target({ElementType.METHOD, ElementType.ANNOTATION_TYPE})
@Retention(RetentionPolicy.RUNTIME)
public @interface GRpcCall {

    @AliasFor("value")
    String name() default "";

    @AliasFor("name")
    String value() default "";

    boolean ignoreServiceName() default false;
}
