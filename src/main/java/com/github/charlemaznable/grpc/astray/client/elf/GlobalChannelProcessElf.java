package com.github.charlemaznable.grpc.astray.client.elf;

import com.github.charlemaznable.grpc.astray.client.westcache.WestCacheClientInterceptor;
import com.github.charlemaznable.httpclient.westcache.WestCacheConstant;
import io.grpc.Channel;
import io.grpc.ClientInterceptor;
import io.grpc.ClientInterceptors;
import lombok.NoArgsConstructor;
import lombok.val;

import java.util.List;
import java.util.ServiceLoader;

import static com.github.charlemaznable.core.lang.Listt.newArrayList;
import static lombok.AccessLevel.PRIVATE;

@NoArgsConstructor(access = PRIVATE)
public final class GlobalChannelProcessElf {

    private static final GlobalChannelProcessor instance;

    static {
        instance = findProcessor();
    }

    public static Channel process(Channel channel) {
        return instance.process(channel);
    }

    private static GlobalChannelProcessor findProcessor() {
        val builders = ServiceLoader.load(GlobalChannelProcessor.class).iterator();
        if (!builders.hasNext()) return new DefaultGlobalChannelProcessor();

        val result = builders.next();
        if (builders.hasNext())
            throw new IllegalStateException("Multiple GlobalChannelProcessor Found");
        return result;
    }

    private static final class DefaultGlobalChannelProcessor implements GlobalChannelProcessor {

        private final List<ClientInterceptor> interceptors = newArrayList();

        DefaultGlobalChannelProcessor() {
            if (WestCacheConstant.HAS_WESTCACHE) {
                interceptors.add(new WestCacheClientInterceptor());
            }
        }

        @Override
        public Channel process(Channel channel) {
            return ClientInterceptors.intercept(channel, interceptors);
        }
    }
}
