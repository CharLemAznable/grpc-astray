package com.github.charlemaznable.grpc.astray.server.common;

import lombok.Getter;
import org.springframework.lang.NonNull;
import org.springframework.util.Assert;

import java.io.Serial;
import java.util.Optional;

@Getter
public final class GRpcRuntimeExceptionAdapter extends RuntimeException {

    @Serial
    private static final long serialVersionUID = 9180289944869848308L;

    private final transient Object hint;

    public GRpcRuntimeExceptionAdapter(@NonNull Throwable cause, Object hint) {
        super(null, cause);
        Assert.notNull(cause, () -> "Cause can't be null");
        this.hint = hint;
    }

    public static Throwable unwrap(Throwable t) {
        return Optional.ofNullable(t)
                .filter(GRpcRuntimeExceptionAdapter.class::isInstance)
                .map(Throwable::getCause).orElse(t);
    }

    public static Object getHint(Throwable exc) {
        return Optional.ofNullable(exc)
                .filter(GRpcRuntimeExceptionAdapter.class::isInstance)
                .map(GRpcRuntimeExceptionAdapter.class::cast)
                .map(w -> w.hint).orElse(null);
    }
}
