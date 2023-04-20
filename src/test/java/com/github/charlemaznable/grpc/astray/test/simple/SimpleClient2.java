package com.github.charlemaznable.grpc.astray.test.simple;

import com.github.bingoohuang.westcache.WestCacheable;
import com.github.charlemaznable.grpc.astray.client.GRpcCall;
import com.github.charlemaznable.grpc.astray.client.GRpcChannel;
import com.github.charlemaznable.grpc.astray.client.GRpcChannelBalance;
import com.github.charlemaznable.grpc.astray.client.GRpcChannelBalance.RoundRobinBalancer;
import com.github.charlemaznable.grpc.astray.client.GRpcClient;
import com.github.charlemaznable.grpc.astray.client.GRpcConfigurerWith;
import com.github.charlemaznable.grpc.astray.client.configurer.configservice.GRpcCommonConfig;

import java.util.concurrent.Future;

@GRpcClient("Simple")
@GRpcChannel({"127.0.0.1:7018", "127.0.0.1:7019", "127.0.0.1:7020"})
@GRpcChannelBalance(RoundRobinBalancer.class)
@GRpcConfigurerWith(GRpcCommonConfig.class) // test for ConfigFactory load error
public interface SimpleClient2 {

    String testString(String req);

    boolean testBoolean(boolean req);

    short testShort(short req);

    int testInt(int req);

    long testLong(long req);

    float testFloat(float req);

    double testDouble(double req);

    byte testByte(byte req);

    char testChar(char req);

    SimpleBean testBean(SimpleBean req);

    @WestCacheable
    String testCache(String req);

    @WestCacheable
    @GRpcCall("TestCache")
    Future<String> testCacheFuture(String req);
}
