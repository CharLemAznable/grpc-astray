package com.github.charlemaznable.grpc.astray.client.configurer;

public interface GRpcInitializationConfigurer extends GRpcConfigurer {

    GRpcInitializationConfigurer INSTANCE = new GRpcInitializationConfigurer() {};

    default void setUpBeforeInitialization(Class<?> grpcClass) {
        GRpcInitializationContext.setGRpcClass(grpcClass);
    }

    default void tearDownAfterInitialization(Class<?> grpcClass) {
        GRpcInitializationContext.clearGRpcClass();
    }
}
