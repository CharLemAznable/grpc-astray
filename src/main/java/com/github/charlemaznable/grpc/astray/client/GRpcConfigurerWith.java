package com.github.charlemaznable.grpc.astray.client;

import com.github.charlemaznable.grpc.astray.client.configurer.GRpcConfigurer;

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
public @interface GRpcConfigurerWith {

    Class<? extends GRpcConfigurer> value();
}
