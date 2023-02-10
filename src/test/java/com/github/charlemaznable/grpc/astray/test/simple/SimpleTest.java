package com.github.charlemaznable.grpc.astray.test.simple;

import com.github.charlemaznable.grpc.astray.client.GRpcClientException;
import com.github.charlemaznable.grpc.astray.client.GRpcFactory;
import com.github.charlemaznable.grpc.astray.test.common.TestApplication;
import lombok.val;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.springframework.boot.test.context.SpringBootTest.WebEnvironment.NONE;

@SuppressWarnings("SpringJavaInjectionPointsAutowiringInspection")
@SpringBootTest(classes = {TestApplication.class, SimpleConfig.class}, webEnvironment = NONE,
        args = {"--grpc.nettyServer.primaryListenAddress=127.0.0.1",
                "--grpc.nettyServer.additionalListenAddresses[0]=127.0.0.1:7019",
                "--grpc.nettyServer.additionalListenAddresses[1]=127.0.0.1:7020"})
public class SimpleTest {

    @Autowired
    private SimpleClient client;
    @Autowired
    private SimpleClient2 client2;

    @Test
    public void testSimple() {
        assertEquals("response: Hello GRpc, ^_^", client.testString("Hello GRpc"));
        assertFalse(client.testBoolean(true));
        assertEquals(2, client.testShort((short) 1));
        assertEquals(4, client.testInt(2));
        assertEquals(6, client.testLong(3));
        assertEquals(8, client.testFloat(4));
        assertEquals(10, client.testDouble(5));
        assertEquals(2, client.testByte((byte) 1));
        assertEquals('b', client.testChar('a'));

        val bean = new SimpleBean();
        bean.setContent("Hello GRpc");
        assertEquals("response: Hello GRpc, ^_^", client.testBean(bean).getContent());

        val myClient = GRpcFactory.getClient(SimpleClient.class);
        assertEquals(client, myClient);
        assertEquals(client.hashCode(), myClient.hashCode());
        assertEquals(client.toString(), myClient.toString());

        val respCache1 = client.testCache("req");
        val respCache2 = client.testCache("req");
        assertEquals(respCache1, respCache2);

        assertThrows(GRpcClientException.class,
                () -> GRpcFactory.getClient(ErrorClient.class));
    }

    @Test
    public void testSimple2() {
        assertEquals("response: Hello GRpc, ^_^", client2.testString("Hello GRpc"));
        assertFalse(client2.testBoolean(true));
        assertEquals(2, client2.testShort((short) 1));
        assertEquals(4, client2.testInt(2));
        assertEquals(6, client2.testLong(3));
        assertEquals(8, client2.testFloat(4));
        assertEquals(10, client2.testDouble(5));
        assertEquals(2, client2.testByte((byte) 1));
        assertEquals('b', client2.testChar('a'));

        val bean = new SimpleBean();
        bean.setContent("Hello GRpc");
        assertEquals("response: Hello GRpc, ^_^", client2.testBean(bean).getContent());

        val myClient2 = GRpcFactory.getClient(SimpleClient2.class);
        assertEquals(client2, myClient2);
        assertEquals(client2.hashCode(), myClient2.hashCode());
        assertEquals(client2.toString(), myClient2.toString());

        val respCache1 = client2.testCache("req");
        val respCache2 = client2.testCache("req");
        assertEquals(respCache1, respCache2);
    }
}
