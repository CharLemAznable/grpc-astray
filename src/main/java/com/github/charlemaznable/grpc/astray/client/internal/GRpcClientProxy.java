package com.github.charlemaznable.grpc.astray.client.internal;

import com.github.charlemaznable.configservice.ConfigFactory;
import com.github.charlemaznable.core.context.FactoryContext;
import com.github.charlemaznable.core.lang.BuddyEnhancer;
import com.github.charlemaznable.core.lang.Factory;
import com.github.charlemaznable.core.lang.Reloadable;
import com.github.charlemaznable.grpc.astray.client.GRpcChannel;
import com.github.charlemaznable.grpc.astray.client.GRpcChannelBalance;
import com.github.charlemaznable.grpc.astray.client.GRpcChannelBalance.ChannelBalancer;
import com.github.charlemaznable.grpc.astray.client.GRpcClient;
import com.github.charlemaznable.grpc.astray.client.GRpcClientException;
import com.github.charlemaznable.grpc.astray.client.GRpcConfigurerWith;
import com.github.charlemaznable.grpc.astray.client.configurer.GRpcChannelBalanceConfigurer;
import com.github.charlemaznable.grpc.astray.client.configurer.GRpcChannelBuilderConfigurer;
import com.github.charlemaznable.grpc.astray.client.configurer.GRpcChannelConfigurer;
import com.github.charlemaznable.grpc.astray.client.configurer.GRpcConfigurer;
import com.google.common.cache.LoadingCache;
import io.grpc.Channel;
import io.grpc.ManagedChannelBuilder;
import lombok.NoArgsConstructor;
import lombok.val;

import java.lang.reflect.Method;
import java.util.Arrays;
import java.util.List;
import java.util.function.Function;

import static com.github.charlemaznable.core.lang.Condition.blankThen;
import static com.github.charlemaznable.core.lang.Condition.checkNotEmpty;
import static com.github.charlemaznable.core.lang.Condition.checkNotNull;
import static com.github.charlemaznable.core.lang.Condition.notNullThen;
import static com.github.charlemaznable.core.lang.Condition.nullThen;
import static com.github.charlemaznable.core.lang.Listt.newArrayList;
import static com.github.charlemaznable.core.lang.LoadingCachee.get;
import static com.github.charlemaznable.core.lang.LoadingCachee.simpleCache;
import static com.github.charlemaznable.grpc.astray.client.internal.GRpcClientDummy.log;
import static com.google.common.cache.CacheLoader.from;
import static java.util.Objects.isNull;
import static java.util.Objects.nonNull;
import static lombok.AccessLevel.PRIVATE;
import static org.springframework.core.annotation.AnnotatedElementUtils.getMergedAnnotation;
import static org.springframework.util.ClassUtils.getShortName;

@SuppressWarnings("rawtypes")
public final class GRpcClientProxy implements BuddyEnhancer.Delegate, Reloadable {

    Class clazz;
    Factory factory;
    String serviceName;
    GRpcConfigurer configurer;
    Function<String, Channel> channelBuilder;
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
    public Object invoke(BuddyEnhancer.Invocation invocation) throws Exception {
        val method = invocation.getMethod();
        val args = invocation.getArguments();
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
        this.configurer = Elf.checkConfigurer(this.clazz, this.factory);
        this.channelBuilder = nullThen(Elf.checkChannelBuilder(this.configurer), () ->
                s -> (Channel) ManagedChannelBuilder.forTarget(s).usePlaintext().build());
        this.channelList = checkNotEmpty(Elf.checkChannelTargets(this.configurer, this.clazz),
                new GRpcClientException(this.clazz.getName() + " channel config is empty"))
                .stream().map(this.channelBuilder).toList();
        this.channelBalancer = nullThen(Elf.checkChannelBalancer(
                this.configurer, this.clazz, this.factory), GRpcChannelBalance.RandomBalancer::new);
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

        static GRpcConfigurer checkConfigurer(Class clazz, Factory factory) {
            val configureWith = getMergedAnnotation(clazz, GRpcConfigurerWith.class);
            if (isNull(configureWith)) return null;
            val configurerClass = configureWith.value();
            val configurer = FactoryContext.build(factory, configurerClass);
            if (nonNull(configurer)) return configurer;
            try {
                return ConfigFactory.configLoader(factory).getConfig(configurerClass);
            } catch (Exception e) {
                log.warn("Load GRpcConfigurer by ConfigService with exception: ", e);
                return null;
            }
        }

        static Function<String, Channel> checkChannelBuilder(GRpcConfigurer configurer) {
            return configurer instanceof GRpcChannelBuilderConfigurer builderConfigurer
                    ? builderConfigurer.channelBuilder() : null;
        }

        static List<String> checkChannelTargets(GRpcConfigurer configurer, Class clazz) {
            if (configurer instanceof GRpcChannelConfigurer channelConfigurer)
                return newArrayList(channelConfigurer.targets())
                        .stream().map(GRpcClientDummy::substitute).toList();
            val channelAnno = getMergedAnnotation(clazz, GRpcChannel.class);
            return notNullThen(channelAnno, anno -> Arrays
                    .stream(anno.value()).map(GRpcClientDummy::substitute).toList());
        }

        static ChannelBalancer checkChannelBalancer(GRpcConfigurer configurer, Class clazz, Factory factory) {
            if (configurer instanceof GRpcChannelBalanceConfigurer channelBalanceConfigurer)
                return channelBalanceConfigurer.channelBalancer();
            val channelBalance = getMergedAnnotation(clazz, GRpcChannelBalance.class);
            return notNullThen(channelBalance, anno -> FactoryContext.build(factory, anno.value()));
        }
    }
}
