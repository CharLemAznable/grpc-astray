package com.github.charlemaznable.grpc.astray.server.validation;

import com.github.charlemaznable.grpc.astray.server.GRpcGlobalInterceptor;
import com.github.charlemaznable.grpc.astray.server.invocation.GRpcExceptionHandler;
import com.github.charlemaznable.grpc.astray.server.invocation.GRpcServiceAdvice;
import com.github.charlemaznable.grpc.astray.server.invocation.exception.ConditionalOnMissingExceptionHandler;
import com.github.charlemaznable.grpc.astray.server.invocation.exception.GRpcExceptionScope;
import com.github.charlemaznable.grpc.astray.server.invocation.handle.GRpcHandlingSupport;
import io.grpc.Status;
import lombok.extern.slf4j.Slf4j;
import lombok.val;
import org.springframework.boot.autoconfigure.condition.ConditionalOnBean;
import org.springframework.boot.autoconfigure.condition.ConditionalOnClass;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Lazy;

import javax.validation.ConstraintViolationException;
import javax.validation.Validator;

@SuppressWarnings({"SpringJavaInjectionPointsAutowiringInspection", "SpringFacetCodeInspection"})
@Configuration
@ConditionalOnClass({Validator.class})
@EnableConfigurationProperties(GRpcValidationProperties.class)
public class GRpcValidationConfiguration {

    @Bean
    @ConditionalOnBean(Validator.class)
    @GRpcGlobalInterceptor
    public GRpcValidatingInterceptor validatingInterceptor(
            @Lazy Validator validator,
            GRpcHandlingSupport handlingSupport,
            GRpcValidationProperties validationProperties) {
        return new GRpcValidatingInterceptor(validator,
                handlingSupport, validationProperties);
    }

    @ConditionalOnMissingExceptionHandler(ConstraintViolationException.class)
    @Configuration
    static class DefaultValidationHandlerConfiguration {

        @Slf4j
        @GRpcServiceAdvice
        public static class DefaultValidationErrorHandler {

            @GRpcExceptionHandler
            public Status handle(ConstraintViolationException e, GRpcExceptionScope scope) {
                val status = scope.getHintAs(Status.class).orElse(Status.UNKNOWN);
                log.error("Got error with status {} ", status.getCode().name(), e);
                return status.withDescription(e.getMessage());
            }
        }
    }
}
