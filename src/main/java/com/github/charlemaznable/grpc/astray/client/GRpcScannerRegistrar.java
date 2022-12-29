package com.github.charlemaznable.grpc.astray.client;

import com.github.charlemaznable.core.spring.SpringFactoryBean;
import com.github.charlemaznable.core.spring.SpringScannerRegistrar;
import com.github.charlemaznable.grpc.astray.client.GRpcFactory.GRpcLoader;
import org.springframework.core.type.ClassMetadata;

import static com.github.charlemaznable.grpc.astray.client.GRpcFactory.springGRpcLoader;

public final class GRpcScannerRegistrar extends SpringScannerRegistrar {

    private static final GRpcLoader springGRpcLoader = springGRpcLoader();

    public GRpcScannerRegistrar() {
        super(GRpcScan.class, GRpcClientFactoryBean.class, GRpcClient.class);
    }

    @Override
    protected boolean isCandidateClass(ClassMetadata classMetadata) {
        return classMetadata.isInterface();
    }

    public static class GRpcClientFactoryBean extends SpringFactoryBean {

        public GRpcClientFactoryBean() {
            super(springGRpcLoader::getClient);
        }
    }
}
