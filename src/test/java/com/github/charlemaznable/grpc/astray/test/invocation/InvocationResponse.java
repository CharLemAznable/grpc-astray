package com.github.charlemaznable.grpc.astray.test.invocation;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@AllArgsConstructor
@NoArgsConstructor
@Getter
@Setter
public class InvocationResponse {

    private int code;

    private String message;

    public static class SuccessResponse extends InvocationResponse {
    }
}
