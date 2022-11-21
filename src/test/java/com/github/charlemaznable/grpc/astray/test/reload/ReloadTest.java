package com.github.charlemaznable.grpc.astray.test.reload;

import com.github.charlemaznable.grpc.astray.test.common.TestApplication;
import io.grpc.Status;
import io.grpc.StatusRuntimeException;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;

import static com.github.charlemaznable.core.lang.Listt.newArrayList;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.springframework.boot.test.context.SpringBootTest.WebEnvironment.NONE;

@SuppressWarnings("SpringJavaInjectionPointsAutowiringInspection")
@SpringBootTest(classes = {TestApplication.class, ReloadConfig.class}, webEnvironment = NONE,
        args = {"--grpc.embeddedServerName=reload"})
public class ReloadTest {

    @Autowired
    private ReloadClient client;
    @Autowired
    private ReloadChannelProvider reloadChannelProvider;

    @Test
    public void testReload() {
        try {
            client.test("Hello GRpc");
        } catch (StatusRuntimeException e) {
            assertEquals(Status.Code.UNAVAILABLE, e.getStatus().getCode());
            assertEquals("Could not find server: none", e.getStatus().getDescription());
        }

        reloadChannelProvider.setNames(newArrayList("reload", "reload"));
        client.reload();
        assertEquals("response: Hello GRpc, ^_^", client.test("Hello GRpc"));
    }
}
