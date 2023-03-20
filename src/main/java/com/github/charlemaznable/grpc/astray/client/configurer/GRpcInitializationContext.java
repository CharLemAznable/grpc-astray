package com.github.charlemaznable.grpc.astray.client.configurer;

import lombok.NoArgsConstructor;

import static lombok.AccessLevel.PRIVATE;

@NoArgsConstructor(access = PRIVATE)
public final class GRpcInitializationContext {

    private static final ThreadLocal<Class<?>> grpcClassLocal = new InheritableThreadLocal<>();

    public static void setGRpcClass(Class<?> grpcClass) {
        grpcClassLocal.set(grpcClass);
    }

    public static Class<?> getGRpcClass() {
        return grpcClassLocal.get();
    }

    public static void clearGRpcClass() {
        grpcClassLocal.remove();
    }
}
