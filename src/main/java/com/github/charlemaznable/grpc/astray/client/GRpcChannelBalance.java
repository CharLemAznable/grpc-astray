package com.github.charlemaznable.grpc.astray.client;

import io.grpc.Channel;

import java.lang.annotation.Documented;
import java.lang.annotation.ElementType;
import java.lang.annotation.Inherited;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;
import java.util.List;
import java.util.concurrent.atomic.AtomicInteger;

import static com.github.charlemaznable.core.lang.Condition.checkNotEmpty;
import static com.github.charlemaznable.core.lang.Rand.randInt;

@Documented
@Inherited
@Target({ElementType.TYPE})
@Retention(RetentionPolicy.RUNTIME)
public @interface GRpcChannelBalance {

    Class<? extends ChannelBalancer> value();

    interface ChannelBalancer {

        Channel choose(List<Channel> channelList);
    }

    class RandomBalancer implements ChannelBalancer {

        @Override
        public Channel choose(List<Channel> channelList) {
            checkNotEmpty(channelList);
            if (1 == channelList.size()) return channelList.get(0);
            return channelList.get(randInt(channelList.size()));
        }
    }

    class RoundRobinBalancer implements ChannelBalancer {

        private final AtomicInteger cyclicCounter = new AtomicInteger(0);

        @Override
        public Channel choose(List<Channel> channelList) {
            checkNotEmpty(channelList);
            if (1 == channelList.size()) return channelList.get(0);
            return channelList.get(getAndIncrementMod(channelList.size()));
        }

        private int getAndIncrementMod(int size) {
            while (true) {
                int curr = cyclicCounter.get();
                int next = (curr + 1) % size;
                if (cyclicCounter.compareAndSet(curr, next)) return next;
            }
        }
    }
}
