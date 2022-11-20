package com.github.charlemaznable.grpc.astray.test.invocation;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.Setter;

@AllArgsConstructor
@Getter
@Setter
public class InvocationRequest {

    private String tenant;

    private String content;
}
