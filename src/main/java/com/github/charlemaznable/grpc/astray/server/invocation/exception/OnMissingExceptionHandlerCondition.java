package com.github.charlemaznable.grpc.astray.server.invocation.exception;

import com.github.charlemaznable.core.lang.ClzPath;
import com.github.charlemaznable.grpc.astray.server.GRpcService;
import com.github.charlemaznable.grpc.astray.server.invocation.GRpcExceptionHandler;
import com.github.charlemaznable.grpc.astray.server.invocation.GRpcServiceAdvice;
import lombok.val;
import org.springframework.boot.autoconfigure.condition.ConditionOutcome;
import org.springframework.boot.autoconfigure.condition.SpringBootCondition;
import org.springframework.context.annotation.ConditionContext;
import org.springframework.core.MethodIntrospector;
import org.springframework.core.type.AnnotatedTypeMetadata;
import org.springframework.util.ReflectionUtils;

import static com.github.charlemaznable.core.lang.Condition.checkNotNull;
import static com.github.charlemaznable.grpc.astray.server.invocation.handle.GRpcHandlerMethodElf.parseHandledException;
import static org.springframework.core.annotation.AnnotatedElementUtils.hasAnnotation;

final class OnMissingExceptionHandlerCondition extends SpringBootCondition {

    @SuppressWarnings("unchecked")
    @Override
    public ConditionOutcome getMatchOutcome(ConditionContext context,
                                            AnnotatedTypeMetadata metadata) {
        val attr = metadata.getAnnotationAttributes(ConditionalOnMissingExceptionHandler.class.getName());
        val exceptionType = (Class<? extends Throwable>) checkNotNull(attr).get("value");
        val beanFactory = checkNotNull(context.getBeanFactory());
        ReflectionUtils.MethodFilter filter = method -> hasAnnotation(method, GRpcExceptionHandler.class);

        for (val adviceBeanName : beanFactory.getBeanNamesForAnnotation(GRpcServiceAdvice.class)) {
            val beanClassName = beanFactory.getBeanDefinition(adviceBeanName).getBeanClassName();
            val beanClass = checkNotNull(ClzPath.findClass(beanClassName),
                    new IllegalStateException("Class Not Found: " + beanClassName));
            // iterate global GRpcServiceAdvice, which except beans annotated with GRpcService.
            if (hasAnnotation(beanClass, GRpcService.class)) continue;

            for (val method : MethodIntrospector.selectMethods(beanClass, filter)) {
                val handledException = parseHandledException(method);
                if (handledException.map(t -> t.isAssignableFrom(exceptionType)).orElse(Boolean.FALSE)) {
                    return ConditionOutcome.noMatch(String.format("Found %s handler at %s.%s",
                            handledException.get().getName(), beanClassName, method.getName()));
                }
            }
        }
        return ConditionOutcome.match();
    }
}
