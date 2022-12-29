package com.github.charlemaznable.grpc.astray.client.internal;

import com.github.charlemaznable.core.context.FactoryContext;
import com.github.charlemaznable.core.lang.Factory;
import com.github.charlemaznable.core.lang.Reloadable;
import com.github.charlemaznable.grpc.astray.client.GRpcChannel;
import com.github.charlemaznable.grpc.astray.client.GRpcChannel.ChannelProvider;
import com.github.charlemaznable.grpc.astray.client.GRpcChannelBalance;
import com.github.charlemaznable.grpc.astray.client.GRpcChannelBalance.ChannelBalancer;
import com.github.charlemaznable.grpc.astray.client.GRpcChannelBalance.RandomBalancer;
import com.github.charlemaznable.grpc.astray.client.GRpcClient;
import com.github.charlemaznable.grpc.astray.client.GRpcClientException;
import com.google.common.cache.LoadingCache;
import io.grpc.Channel;
import io.grpc.ManagedChannelBuilder;
import lombok.NoArgsConstructor;
import lombok.val;
import net.sf.cglib.proxy.MethodInterceptor;
import net.sf.cglib.proxy.MethodProxy;

import java.lang.reflect.Method;
import java.util.List;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import static com.github.charlemaznable.core.lang.Condition.blankThen;
import static com.github.charlemaznable.core.lang.Condition.checkNotEmpty;
import static com.github.charlemaznable.core.lang.Condition.checkNotNull;
import static com.github.charlemaznable.core.lang.Condition.checkNull;
import static com.github.charlemaznable.core.lang.LoadingCachee.get;
import static com.github.charlemaznable.core.lang.LoadingCachee.simpleCache;
import static com.google.common.cache.CacheLoader.from;
import static lombok.AccessLevel.PRIVATE;
import static org.springframework.core.annotation.AnnotatedElementUtils.getMergedAnnotation;
import static org.springframework.util.ClassUtils.getShortName;

@SuppressWarnings("rawtypes")
public final class GRpcClientProxy implements MethodInterceptor, Reloadable {

    Class clazz;
    Factory factory;
    String serviceName;
    List<Channel> channelList;
    ChannelBalancer channelBalancer;

    LoadingCache<Method, GRpcCallProxy> callProxyCache
            = simpleCache(from(this::loadCallProxy));

    public GRpcClientProxy(Class clazz, Factory factory) {
        this.clazz = clazz;
        this.factory = factory;
        this.initialize();
    }

    @Override
    public Object intercept(Object o, Method method, Object[] args,
                            MethodProxy methodProxy) throws Throwable {
        if (method.getDeclaringClass().equals(Reloadable.class)) {
            return method.invoke(this, args);
        }

        val callProxy = get(callProxyCache, method);
        val channel = this.channelBalancer.choose(this.channelList);
        return callProxy.execute(channel, args);
    }

    @Override
    public void reload() {
        this.initialize();
        this.callProxyCache.invalidateAll();
    }

    private void initialize() {
        this.serviceName = Elf.checkGRpcServiceName(this.clazz);
        this.channelList = Elf.checkChannelList(this.clazz, this.factory);
        this.channelBalancer = Elf.checkChannelBalancer(this.clazz, this.factory);
    }

    private GRpcCallProxy loadCallProxy(Method method) {
        return new GRpcCallProxy(method, this);
    }

    @NoArgsConstructor(access = PRIVATE)
    static class Elf {

        static String checkGRpcServiceName(Class clazz) {
            val grpcClientAnno = getMergedAnnotation(clazz, GRpcClient.class);
            checkNotNull(grpcClientAnno, new GRpcClientException(
                    clazz.getName() + " has no GRpcClient annotation"));
            return blankThen(grpcClientAnno.value(), () -> getShortName(clazz));
        }

        static List<Channel> checkChannelList(Class clazz, Factory factory) {
            val channelAnno = getMergedAnnotation(clazz, GRpcChannel.class);
            checkNotNull(channelAnno, new GRpcClientException(
                    clazz.getName() + " has no GRpcChannel annotation"));
            val providerClass = channelAnno.channelProvider();
            List<Channel> channelList = (ChannelProvider.class == providerClass
                    ? Stream.of(channelAnno.value()).map(GRpcClientDummy::substitute)
                    .map(s -> (Channel) ManagedChannelBuilder.forTarget(s).usePlaintext().build())
                    .collect(Collectors.toList())
                    : FactoryContext.apply(factory, providerClass, p -> p.channels(clazz)));
            return checkNotEmpty(channelList, new GRpcClientException(
                    clazz.getName() + " channel config is empty"));
        }

        static ChannelBalancer checkChannelBalancer(Class clazz, Factory factory) {
            val channelBalance = getMergedAnnotation(clazz, GRpcChannelBalance.class);
            return checkNull(channelBalance, RandomBalancer::new, annotation ->
                    FactoryContext.build(factory, annotation.value()));
        }
    }
}
