package com.github.charlemaznable.grpc.astray.test.validation;

import jakarta.validation.constraints.NotBlank;
import lombok.Getter;
import lombok.Setter;
import lombok.experimental.Accessors;

@Getter
@Setter
@Accessors(chain = true)
public class ValidationResponse {

    @NotBlank(message = "返回信息不能为空")
    private String message;
}
