package com.github.charlemaznable.grpc.astray.client;

import com.github.charlemaznable.core.spring.SpringFactoryBean;
import com.github.charlemaznable.core.spring.SpringScannerRegistrar;
import com.github.charlemaznable.grpc.astray.client.GRpcFactory.GRpcLoader;
import lombok.Setter;
import org.springframework.beans.factory.config.BeanDefinition;
import org.springframework.core.type.ClassMetadata;

import static com.github.charlemaznable.grpc.astray.client.GRpcFactory.springGRpcLoader;

public final class GRpcScannerRegistrar extends SpringScannerRegistrar {

    private final GRpcLoader loader;

    public GRpcScannerRegistrar() {
        super(GRpcScan.class, GRpcClientFactoryBean.class, GRpcClient.class);
        this.loader = springGRpcLoader();
    }

    @Override
    protected boolean isCandidateClass(ClassMetadata classMetadata) {
        return classMetadata.isInterface();
    }

    protected void postProcessBeanDefinition(BeanDefinition beanDefinition) {
        super.postProcessBeanDefinition(beanDefinition);
        beanDefinition.getPropertyValues().add("loader", this.loader);
    }

    public static class GRpcClientFactoryBean extends SpringFactoryBean {

        @Setter
        private GRpcLoader loader;

        @Override
        public Object buildObject(Class<?> xyzInterface) {
            return loader.getClient(xyzInterface);
        }
    }
}
