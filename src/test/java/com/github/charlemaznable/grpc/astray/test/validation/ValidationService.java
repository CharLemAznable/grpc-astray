package com.github.charlemaznable.grpc.astray.test.validation;

import com.github.charlemaznable.grpc.astray.server.GRpcMethod;
import com.github.charlemaznable.grpc.astray.server.GRpcService;

@GRpcService("Validation")
public class ValidationService {

    @GRpcMethod
    public ValidationResponse call(ValidationRequest req) {
        if ("John".equals(req.getName())) {
            return new ValidationResponse();
        } else {
            return new ValidationResponse().setMessage("校验成功");
        }
    }
}
