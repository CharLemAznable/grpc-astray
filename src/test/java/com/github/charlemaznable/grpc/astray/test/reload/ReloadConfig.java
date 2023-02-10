package com.github.charlemaznable.grpc.astray.test.reload;

import com.github.charlemaznable.grpc.astray.client.GRpcScan;
import org.n3r.diamond.client.impl.MockDiamondServer;
import org.springframework.context.annotation.ComponentScan;
import org.springframework.context.annotation.Configuration;

import javax.annotation.PostConstruct;
import javax.annotation.PreDestroy;

import static com.github.charlemaznable.configservice.diamond.DiamondFactory.diamondLoader;
import static com.github.charlemaznable.core.spring.SpringFactory.springFactory;
import static org.joor.Reflect.on;

@Configuration
@ComponentScan
@GRpcScan
public class ReloadConfig {

    @PostConstruct
    public void postConstruct() {
        on(diamondLoader(springFactory())).field("configCache").call("invalidateAll");
        MockDiamondServer.setUpMockServer();
        MockDiamondServer.setConfigInfo("GRPC", "reload", "" +
                        "targets=none\n" +
                        "channelBalancer=randomm\n");
    }

    @PreDestroy
    public void preDestroy() {
        MockDiamondServer.tearDownMockServer();
    }
}
