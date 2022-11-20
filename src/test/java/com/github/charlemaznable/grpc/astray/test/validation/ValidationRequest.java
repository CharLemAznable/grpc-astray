package com.github.charlemaznable.grpc.astray.test.validation;

import lombok.Getter;
import lombok.Setter;
import lombok.experimental.Accessors;

import javax.validation.constraints.NotBlank;

@Getter
@Setter
@Accessors(chain = true)
public class ValidationRequest {

    @NotBlank(message = "用户名不能为空")
    private String name;

    @NotBlank(message = "邮箱不能为空")
    private String email;
}
