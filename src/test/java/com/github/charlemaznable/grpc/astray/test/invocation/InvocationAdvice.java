package com.github.charlemaznable.grpc.astray.test.invocation;

import com.github.charlemaznable.grpc.astray.server.invocation.GRpcCleanupHandler;
import com.github.charlemaznable.grpc.astray.server.invocation.GRpcExceptionHandler;
import com.github.charlemaznable.grpc.astray.server.invocation.GRpcRequestHandler;
import com.github.charlemaznable.grpc.astray.server.invocation.GRpcResponseHandler;
import com.github.charlemaznable.grpc.astray.server.invocation.GRpcServiceAdvice;
import com.github.charlemaznable.grpc.astray.server.invocation.exception.GRpcExceptionScope;
import io.grpc.Status;

@GRpcServiceAdvice
public class InvocationAdvice {

    @GRpcRequestHandler
    public InvocationRequest requestHandler(InvocationRequest req) {
        InvocationContext.setTenant(req.getTenant());
        return req;
    }

    @GRpcResponseHandler
    public InvocationResponse responseHandler(InvocationResponse rsp) {
        return new InvocationResponse("SUCCESS".equals(rsp.getMessage()) ? 0 : rsp.getCode(), rsp.getMessage());
    }

    @GRpcResponseHandler
    public InvocationResponse successHandler(InvocationResponse.SuccessResponse rsp) {
        rsp.setCode(0);
        rsp.setMessage("SUCCESS");
        return rsp;
    }

    @GRpcExceptionHandler
    public Status exceptionHandler(IllegalStateException ex, GRpcExceptionScope scope) {
        scope.getRecovery().setResponse(new InvocationResponse(-3, "RuntimeException"));
        return Status.OK;
    }

    @GRpcCleanupHandler
    public void cleanupHandler() {
        InvocationContext.reset();
    }
}
