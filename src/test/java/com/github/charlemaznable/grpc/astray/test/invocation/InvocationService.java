package com.github.charlemaznable.grpc.astray.test.invocation;

import com.github.charlemaznable.grpc.astray.server.GRpcMethod;
import com.github.charlemaznable.grpc.astray.server.GRpcService;
import com.github.charlemaznable.grpc.astray.server.invocation.GRpcExceptionHandler;
import com.github.charlemaznable.grpc.astray.server.invocation.exception.GRpcExceptionScope;
import io.grpc.Status;

import static org.junit.jupiter.api.Assertions.assertEquals;

@GRpcService("Invocation")
public class InvocationService {

    @GRpcMethod("Call")
    public InvocationResponse call(InvocationRequest req) {
        assertEquals(req.getTenant(), InvocationContext.getTenant());

        if ("success".equals(req.getContent())) {
            return new InvocationResponse.SuccessResponse();
        } else if ("failed".equals(req.getContent())) {
            return new InvocationResponse(-1, "FAILED");
        } else if ("exception".equals(req.getContent())) {
            throw new IllegalStateException("EXCEPTION");
        } else {
            throw new IllegalArgumentException(new NullPointerException());
        }
    }

    @GRpcExceptionHandler
    public Status npeHandler(NullPointerException npe, GRpcExceptionScope scope) {
        scope.getRecovery().setResponse(new InvocationResponse(-2, "NPE"));
        return Status.OK;
    }
}
