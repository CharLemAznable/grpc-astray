package com.github.charlemaznable.grpc.astray.test.invocation;

public class InvocationContext {

    private static final ThreadLocal<String> tenantLocal = new InheritableThreadLocal<>();

    public static void setTenant(String tenant) {
        tenantLocal.set(tenant);
    }

    public static String getTenant() {
        return tenantLocal.get();
    }

    public static void reset() {
        tenantLocal.remove();
    }
}
