package com.github.charlemaznable.grpc.astray.client.internal;

import com.github.charlemaznable.grpc.astray.client.GRpcCall;
import com.github.charlemaznable.grpc.astray.client.GRpcClientException;
import io.grpc.CallOptions;
import io.grpc.Channel;
import io.grpc.MethodDescriptor;
import io.grpc.MethodDescriptor.MethodType;
import lombok.NoArgsConstructor;
import lombok.val;

import java.lang.reflect.Method;
import java.lang.reflect.ParameterizedType;
import java.lang.reflect.Type;
import java.util.concurrent.Future;

import static com.github.charlemaznable.core.lang.Condition.blankThen;
import static com.github.charlemaznable.core.lang.Str.isBlank;
import static com.github.charlemaznable.grpc.astray.common.GRpcAstrayMarshaller.jsonMarshaller;
import static io.grpc.stub.ClientCalls.blockingUnaryCall;
import static io.grpc.stub.ClientCalls.futureUnaryCall;
import static java.util.Objects.isNull;
import static lombok.AccessLevel.PRIVATE;
import static org.springframework.core.annotation.AnnotatedElementUtils.getMergedAnnotation;
import static org.springframework.util.StringUtils.capitalize;

public final class GRpcCallProxy {

    Method method;
    boolean returnFuture; // Future<V>
    Class<?> returnType;
    MethodDescriptor<Object, Object> methodDescriptor;

    public GRpcCallProxy(Method method, GRpcClientProxy proxy) {
        this.method = method;
        processReturnType(this.method);
        this.methodDescriptor = Elf.checkGRpcMethodDescriptor(
                this.method, proxy, this.returnType);
    }

    private void processReturnType(Method method) {
        Class<?> rt = method.getReturnType();
        this.returnFuture = Future.class == rt;

        val genericReturnType = method.getGenericReturnType();
        if (!(genericReturnType instanceof ParameterizedType)) {
            // 错误的泛型时
            if (this.returnFuture)
                throw new GRpcClientException("Method return type generic Error");
            else this.returnType = rt;
            return;
        }

        // 方法返回泛型时
        ParameterizedType parameterizedType = (ParameterizedType) genericReturnType;
        Type[] actualTypeArguments = parameterizedType.getActualTypeArguments();
        if (this.returnFuture) {
            // 返回Future类型, 则多处理一层泛型
            val futureTypeArgument = actualTypeArguments[0];
            if (!(futureTypeArgument instanceof ParameterizedType)) {
                this.returnType = (Class) futureTypeArgument;
                return;
            }
            parameterizedType = (ParameterizedType) futureTypeArgument;
            rt = (Class) parameterizedType.getRawType();
        }
        this.returnType = rt;
    }

    Object execute(Channel channel, Object[] args) {
        if (isNull(this.methodDescriptor) || 1 != args.length) return null;
        if (this.returnFuture) {
            return futureUnaryCall(channel.newCall(
                    this.methodDescriptor, CallOptions.DEFAULT), args[0]);
        }
        return blockingUnaryCall(channel,
                this.methodDescriptor, CallOptions.DEFAULT, args[0]);
    }

    @NoArgsConstructor(access = PRIVATE)
    static class Elf {

        static MethodDescriptor<Object, Object> checkGRpcMethodDescriptor(Method method,
                                                                          GRpcClientProxy proxy,
                                                                          Class<?> returnType) {
            if (void.class == returnType || Void.class == returnType) return null;
            val parameters = method.getParameters();
            if (1 != parameters.length) return null;
            val grpcMethodName = checkGRpcMethodName(method, proxy.serviceName);
            if (isBlank(grpcMethodName)) return null;
            return MethodDescriptor.newBuilder()
                    .setType(MethodType.UNARY)
                    .setFullMethodName(grpcMethodName)
                    .setSampledToLocalTracing(true)
                    .setRequestMarshaller(jsonMarshaller(method.getParameterTypes()[0]))
                    .setResponseMarshaller(jsonMarshaller(returnType))
                    .build();
        }

        private static String checkGRpcMethodName(Method method, String grpcServiceName) {
            val defaultMethodName = capitalize(method.getName());
            val grpcCallAnno = getMergedAnnotation(method, GRpcCall.class);
            if (isNull(grpcCallAnno)) return grpcServiceName + "/" + defaultMethodName;
            val methodName = blankThen(grpcCallAnno.value(), () -> defaultMethodName);
            return grpcCallAnno.ignoreServiceName() ? methodName : grpcServiceName + "/" + methodName;
        }
    }
}
