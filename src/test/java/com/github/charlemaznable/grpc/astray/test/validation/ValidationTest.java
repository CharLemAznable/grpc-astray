package com.github.charlemaznable.grpc.astray.test.validation;

import com.github.charlemaznable.grpc.astray.test.common.TestApplication;
import io.grpc.Status;
import io.grpc.StatusRuntimeException;
import lombok.val;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.springframework.boot.test.context.SpringBootTest.WebEnvironment.NONE;

@SuppressWarnings("SpringJavaInjectionPointsAutowiringInspection")
@SpringBootTest(classes = {TestApplication.class, ValidationConfig.class}, webEnvironment = NONE,
        args = {"--grpc.embeddedServerName=validation"})
public class ValidationTest {

    @Autowired
    private ValidationClient client;

    @Test
    public void testValidation() {
        val rsp1 = client.call(new ValidationRequest().setName("Tom").setEmail("tom123@gmail.com"));
        assertEquals("校验成功", rsp1.getMessage());

        try {
            client.call(new ValidationRequest().setName("Tom"));
        } catch (StatusRuntimeException e) {
            assertEquals(Status.Code.INVALID_ARGUMENT, e.getStatus().getCode());
            assertEquals("email: 邮箱不能为空", e.getStatus().getDescription());
        }

        try {
            client.call(new ValidationRequest().setEmail("tom123@gmail.com"));
        } catch (StatusRuntimeException e) {
            assertEquals(Status.Code.INVALID_ARGUMENT, e.getStatus().getCode());
            assertEquals("name: 用户名不能为空", e.getStatus().getDescription());
        }

        try {
            client.call(new ValidationRequest().setName("John").setEmail("john456@gmail.com"));
        } catch (StatusRuntimeException e) {
            assertEquals(Status.Code.FAILED_PRECONDITION, e.getStatus().getCode());
            assertEquals("message: 返回信息不能为空", e.getStatus().getDescription());
        }
    }
}
