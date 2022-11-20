package com.github.charlemaznable.grpc.astray.test.invocation;

import com.github.charlemaznable.grpc.astray.test.common.TestApplication;
import lombok.val;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.springframework.boot.test.context.SpringBootTest.WebEnvironment.NONE;

@SuppressWarnings("SpringJavaInjectionPointsAutowiringInspection")
@SpringBootTest(classes = {TestApplication.class, InvocationConfig.class}, webEnvironment = NONE,
        args = {"--grpc.embeddedServerName=invocation"})
public class InvocationTest {

    @Autowired
    private InvocationClient client;

    @Test
    public void testInvocation() {
        val rsp1 = client.call(new InvocationRequest("tenant1", "success"));
        assertEquals(0, rsp1.getCode());
        assertEquals("SUCCESS", rsp1.getMessage());

        val rsp2 = client.call(new InvocationRequest("tenant2", "failed"));
        assertEquals(-1, rsp2.getCode());
        assertEquals("FAILED", rsp2.getMessage());

        val rsp3 = client.call(new InvocationRequest("tenant3", "exception"));
        assertEquals(-3, rsp3.getCode());
        assertEquals("RuntimeException", rsp3.getMessage());

        val rsp4 = client.call(new InvocationRequest("tenant3", "unknown"));
        assertEquals(-2, rsp4.getCode());
        assertEquals("NPE", rsp4.getMessage());
    }
}
