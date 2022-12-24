package com.github.charlemaznable.grpc.astray.common;

import com.github.charlemaznable.core.lang.Closer;
import com.google.common.primitives.Primitives;
import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;
import io.grpc.MethodDescriptor.Marshaller;
import lombok.extern.slf4j.Slf4j;
import lombok.val;
import org.apache.commons.io.IOUtils;

import javax.annotation.Nullable;
import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.io.InputStream;

import static com.github.charlemaznable.core.codec.Bytes.bytes;
import static com.github.charlemaznable.core.codec.Bytes.string;
import static com.github.charlemaznable.core.lang.Condition.notNullThen;
import static org.apache.commons.lang3.BooleanUtils.toBoolean;
import static org.apache.commons.lang3.math.NumberUtils.toByte;
import static org.apache.commons.lang3.math.NumberUtils.toDouble;
import static org.apache.commons.lang3.math.NumberUtils.toFloat;
import static org.apache.commons.lang3.math.NumberUtils.toInt;
import static org.apache.commons.lang3.math.NumberUtils.toLong;
import static org.apache.commons.lang3.math.NumberUtils.toShort;

@Slf4j(topic = "grpc.service.marshaller")
public abstract class GRpcAstrayMarshaller implements Marshaller<Object> {

    public static GRpcAstrayMarshaller jsonMarshaller(Class<?> type) {
        return new GRpcJsonMarshaller(type);
    }

    public abstract byte[] encode(Object obj);

    public abstract Object decode(byte[] bytes);

    @Override
    public InputStream stream(Object obj) {
        return new ByteArrayInputStream(encode(obj));
    }

    @Override
    public Object parse(InputStream inputStream) {
        return notNullThen(parseInternal(inputStream), this::decode);
    }

    @Nullable
    private static byte[] parseInternal(InputStream stream) {
        try {
            return IOUtils.toByteArray(stream);
        } catch (IOException e) {
            log.warn("Read byte from stream error: ", e);
            return null;
        } finally {
            Closer.closeQuietly(stream);
        }
    }

    protected static Object parsePrimitive(Class<?> clazz, String value) {
        if (clazz == String.class) return value;
        if (clazz == boolean.class) return toBoolean(value);
        if (clazz == short.class) return toShort(value);
        if (clazz == int.class) return toInt(value);
        if (clazz == long.class) return toLong(value);
        if (clazz == float.class) return toFloat(value);
        if (clazz == double.class) return toDouble(value);
        if (clazz == byte.class) return toByte(value);
        if (clazz == char.class) return value.length() > 0 ? value.charAt(0) : '\0';
        return null;
    }

    private static class GRpcJsonMarshaller extends GRpcAstrayMarshaller {

        private final Class<?> clazz;
        private final boolean isPrimitiveOrString;
        private final Gson gson;

        GRpcJsonMarshaller(Class<?> clazz) {
            this.clazz = Primitives.unwrap(clazz);
            this.isPrimitiveOrString = this.clazz.isPrimitive() || String.class == this.clazz;
            this.gson = new Gson();
        }

        @Override
        public byte[] encode(Object obj) {
            if (isPrimitiveOrString) return bytes(obj.toString());
            return bytes(gson.toJson(obj));
        }

        @Override
        public Object decode(byte[] bytes) {
            val string = string(bytes);
            if (isPrimitiveOrString) return parsePrimitive(clazz, string);
            return gson.fromJson(string, TypeToken.get(clazz).getType());
        }
    }
}
