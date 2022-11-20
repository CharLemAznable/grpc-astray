package com.github.charlemaznable.grpc.astray.server;

import io.grpc.ServerInterceptor;
import org.springframework.core.annotation.AliasFor;
import org.springframework.stereotype.Service;

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
@Service
public @interface GRpcService {

    @AliasFor("value")
    String name() default "";

    @AliasFor("name")
    String value() default "";

    Class<? extends ServerInterceptor>[] interceptors() default {};

    boolean applyGlobalInterceptors() default true;

    @AliasFor(annotation = Service.class, value = "value")
    String beanName() default "";
}
