package com.github.charlemaznable.grpc.astray.test.interceptor;

import com.github.charlemaznable.grpc.astray.test.common.TestApplication;
import lombok.SneakyThrows;
import lombok.val;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;

import static org.awaitility.Awaitility.await;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.springframework.boot.test.context.SpringBootTest.WebEnvironment.NONE;

@SuppressWarnings("SpringJavaInjectionPointsAutowiringInspection")
@SpringBootTest(classes = {TestApplication.class, InterceptorConfig.class}, webEnvironment = NONE,
        args = {"--grpc.embeddedServerName=interceptor"})
public class InterceptorTest {

    @Autowired
    private InterceptorClient client;
    @Autowired
    private InterceptorLogger logger;

    @SneakyThrows
    @Test
    public void testInterceptor() {
        val future = client.test("Hello GRpc");
        await().until(future::isDone);
        assertEquals("intercepted response: intercepted Hello GRpc, ^_^", future.get());
        assertEquals("intercepted Hello GRpc", logger.getLogRequest());
        assertEquals("response: intercepted Hello GRpc, ^_^", logger.getLogResponse());
    }
}
