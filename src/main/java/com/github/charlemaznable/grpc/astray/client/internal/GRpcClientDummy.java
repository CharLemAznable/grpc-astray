package com.github.charlemaznable.grpc.astray.client.internal;

import lombok.NoArgsConstructor;
import org.apache.commons.text.StringSubstitutor;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.Properties;

import static com.github.charlemaznable.core.config.Arguments.argumentsAsProperties;
import static com.github.charlemaznable.core.lang.ClzPath.classResourceAsProperties;
import static com.github.charlemaznable.core.lang.Propertiess.ssMap;
import static java.util.Objects.isNull;

@NoArgsConstructor
public class GRpcClientDummy {

    static final Logger log = LoggerFactory.getLogger("GRpcClient");
    static Properties grpcClassPathProperties;

    static String substitute(String source) {
        if (isNull(grpcClassPathProperties)) {
            grpcClassPathProperties = classResourceAsProperties("grpc.client.env.props");
        }
        return new StringSubstitutor(ssMap(argumentsAsProperties(
                grpcClassPathProperties))).replace(source);
    }

    @Override
    public boolean equals(Object obj) {
        return obj instanceof GRpcClientDummy && hashCode() == obj.hashCode();
    }

    @Override
    public int hashCode() {
        return System.identityHashCode(this);
    }

    @Override
    public String toString() {
        return "GRpcClient@" + Integer.toHexString(hashCode());
    }
}
