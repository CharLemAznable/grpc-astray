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
@Target({ElementType.TYPE})
@Retention(RetentionPolicy.RUNTIME)
public @interface GRpcChannel {

    @AliasFor("targets")
    String[] value() default "";

    @AliasFor("value")
    String[] targets() default "";
}
