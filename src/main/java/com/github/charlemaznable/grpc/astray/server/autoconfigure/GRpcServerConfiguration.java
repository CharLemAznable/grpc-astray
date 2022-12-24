package com.github.charlemaznable.grpc.astray.server.autoconfigure;

import com.github.charlemaznable.gentle.spring.factory.AutoConfigurationImport;
import com.github.charlemaznable.gentle.spring.factory.SpringFactory;
import com.github.charlemaznable.grpc.astray.server.GRpcService;
import com.github.charlemaznable.grpc.astray.server.invocation.GRpcInvocationConfiguration;
import com.github.charlemaznable.grpc.astray.server.validation.GRpcValidationConfiguration;
import io.grpc.ServerBuilder;
import lombok.val;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.boot.autoconfigure.AutoConfigureAfter;
import org.springframework.boot.autoconfigure.AutoConfigureOrder;
import org.springframework.boot.autoconfigure.EnableAutoConfiguration;
import org.springframework.boot.autoconfigure.condition.ConditionalOnBean;
import org.springframework.boot.autoconfigure.condition.ConditionalOnMissingBean;
import org.springframework.boot.autoconfigure.validation.ValidationAutoConfiguration;
import org.springframework.boot.context.properties.ConfigurationPropertiesBinding;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Import;
import org.springframework.context.support.AbstractApplicationContext;
import org.springframework.core.convert.converter.Converter;

import javax.annotation.Nonnull;
import java.net.InetSocketAddress;
import java.util.List;
import java.util.function.Consumer;

@SuppressWarnings({"SpringFacetCodeInspection", "rawtypes"})
@Configuration
@Import({
        GRpcInvocationConfiguration.class,
        GRpcValidationConfiguration.class,
        NettyServerBuilderConfiguration.class,
})
@EnableConfigurationProperties(GRpcServerProperties.class)
@AutoConfigureOrder
@AutoConfigureAfter(ValidationAutoConfiguration.class)
@ConditionalOnBean(annotation = GRpcService.class)
@AutoConfigurationImport
@SpringFactory(EnableAutoConfiguration.class)
public class GRpcServerConfiguration {

    @Bean
    @ConditionalOnGRpcServerEnabled
    public GRpcServerRunner grpcServerRunner(AbstractApplicationContext applicationContext,
                                             GRpcServerProperties gRpcServerProperties,
                                             GRpcServicesRegistry gRpcServicesRegistry,
                                             @Qualifier("grpcInternalConfigurator")
                                                     Consumer<ServerBuilder> configurator,
                                             ServerBuilder serverBuilder) {
        return new GRpcServerRunner(applicationContext, gRpcServerProperties,
                gRpcServicesRegistry, configurator, serverBuilder);
    }

    @Bean
    public GRpcServicesRegistry grpcServicesRegistry() {
        return new GRpcServicesRegistry();
    }

    @Bean(name = "grpcInternalConfigurator")
    public Consumer<ServerBuilder> configurator(List<GRpcServerBuilderConfigurer> configurers,
                                                GRpcServerProperties grpcServerProperties) {
        return serverBuilder -> configurers.forEach(c -> c.configure(serverBuilder));
    }

    @Bean
    @ConditionalOnMissingBean(GRpcServerBuilderConfigurer.class)
    public GRpcServerBuilderConfigurer serverBuilderConfigurer() {
        return serverBuilder -> {};
    }

    @Bean
    @ConfigurationPropertiesBinding
    public Converter<String, InetSocketAddress> socketAddressConverter() {
        return new SocketAddressConverter();
    }

    private static class SocketAddressConverter implements Converter<String, InetSocketAddress> {

        @Override
        public InetSocketAddress convert(@Nonnull String source) {
            val chunks = source.split(":");
            int port;
            switch (chunks.length) {
                case 1 -> port = GRpcServerProperties.DEFAULT_GRPC_PORT;
                case 2 -> {
                    port = Integer.parseInt(chunks[1]);
                    if (port < 1)
                        throw new IllegalArgumentException(source + " socket address port is illegal");
                }
                default -> throw new IllegalArgumentException(source + " can't be converted to socket address");
            }
            return new InetSocketAddress(chunks[0], port);
        }
    }
}
