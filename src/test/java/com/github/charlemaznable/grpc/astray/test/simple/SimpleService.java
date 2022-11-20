package com.github.charlemaznable.grpc.astray.test.simple;

import com.github.charlemaznable.grpc.astray.server.GRpcMethod;
import com.github.charlemaznable.grpc.astray.server.GRpcService;

@GRpcService("Simple")
public class SimpleService {

    @GRpcMethod
    public String testString(String req) {
        return "response: " + req + ", ^_^";
    }

    @GRpcMethod
    public boolean testBoolean(boolean req) {
        return !req;
    }

    @GRpcMethod
    public short testShort(short req) {
        return (short) (req + 1);
    }

    @GRpcMethod
    public int testInt(int req) {
        return req + 2;
    }

    @GRpcMethod
    public long testLong(long req) {
        return req + 3;
    }

    @GRpcMethod
    public float testFloat(float req) {
        return req + 4;
    }

    @GRpcMethod
    public double testDouble(double req) {
        return req + 5;
    }

    @GRpcMethod
    public byte testByte(byte req) {
        return (byte) (req + 1);
    }

    @GRpcMethod
    public char testChar(char req) {
        return (char) (req + 1);
    }

    @GRpcMethod
    public SimpleBean testBean(SimpleBean req) {
        req.setContent("response: " + req.getContent() + ", ^_^");
        return req;
    }
}
