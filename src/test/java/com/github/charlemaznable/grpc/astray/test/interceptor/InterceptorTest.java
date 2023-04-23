package com.github.charlemaznable.grpc.astray.test.interceptor;

import com.github.charlemaznable.grpc.astray.test.common.TestApplication;
import lombok.SneakyThrows;
import lombok.val;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;

import java.util.concurrent.atomic.AtomicBoolean;

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

    @SuppressWarnings("ResultOfMethodCallIgnored")
    @SneakyThrows
    @Test
    public void testInterceptor() {
        val future = client.test("Hello GRpc");
        await().until(future::isDone);
        assertEquals("intercepted response: intercepted Hello GRpc, ^_^", future.get());
        assertEquals("intercepted Hello GRpc", logger.getLogRequest());
        assertEquals("response: intercepted Hello GRpc, ^_^", logger.getLogResponse());

        val finished = new AtomicBoolean();
        client.testMono("Hello GRpc Mono").subscribe(resp -> {
            assertEquals("intercepted response: intercepted Hello GRpc Mono, ^_^", resp);
            assertEquals("intercepted Hello GRpc Mono", logger.getLogRequest());
            assertEquals("response: intercepted Hello GRpc Mono, ^_^", logger.getLogResponse());
            finished.set(true);
        });
        await().forever().until(finished::get);

        finished.set(false);
        client.testRx("Hello GRpc Rx").subscribe(resp -> {
            assertEquals("intercepted response: intercepted Hello GRpc Rx, ^_^", resp);
            assertEquals("intercepted Hello GRpc Rx", logger.getLogRequest());
            assertEquals("response: intercepted Hello GRpc Rx, ^_^", logger.getLogResponse());
            finished.set(true);
        });
        await().forever().until(finished::get);

        finished.set(false);
        client.testRx2("Hello GRpc Rx2").subscribe(resp -> {
            assertEquals("intercepted response: intercepted Hello GRpc Rx2, ^_^", resp);
            assertEquals("intercepted Hello GRpc Rx2", logger.getLogRequest());
            assertEquals("response: intercepted Hello GRpc Rx2, ^_^", logger.getLogResponse());
            finished.set(true);
        });
        await().forever().until(finished::get);

        finished.set(false);
        client.testRx3("Hello GRpc Rx3").subscribe(resp -> {
            assertEquals("intercepted response: intercepted Hello GRpc Rx3, ^_^", resp);
            assertEquals("intercepted Hello GRpc Rx3", logger.getLogRequest());
            assertEquals("response: intercepted Hello GRpc Rx3, ^_^", logger.getLogResponse());
            finished.set(true);
        });
        await().forever().until(finished::get);

        finished.set(false);
        client.testUni("Hello GRpc Uni").subscribe().with(resp -> {
            assertEquals("intercepted response: intercepted Hello GRpc Uni, ^_^", resp);
            assertEquals("intercepted Hello GRpc Uni", logger.getLogRequest());
            assertEquals("response: intercepted Hello GRpc Uni, ^_^", logger.getLogResponse());
            finished.set(true);
        });
        await().forever().until(finished::get);
    }
}
