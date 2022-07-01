package osgi.common.util.eventstream.impl;

import java.time.Duration;

import org.ehcache.Cache;
import org.ehcache.config.builders.CacheConfigurationBuilder;
import org.ehcache.config.builders.ExpiryPolicyBuilder;
import org.ehcache.config.builders.ResourcePoolsBuilder;

import osgi.common.util.EhCacheUtil;
import osgi.common.util.eventstream.api.Event;
import osgi.common.util.eventstream.api.IEventConsumer;
import osgi.common.util.eventstream.api.IPlugin;

public class IgnoreDuplicates implements IPlugin {
    private IEventConsumer nextConsumer;

    Cache<Integer, String> handledEventCache = EhCacheUtil.getManager().createCache("IntToStringCache",
            CacheConfigurationBuilder
                    .newCacheConfigurationBuilder(Integer.class, String.class, ResourcePoolsBuilder.heap(10000)) // adapt to rate of flow
                    .withExpiry(ExpiryPolicyBuilder.timeToLiveExpiration(Duration.ofMillis(5000)))); // adapt to rate of flow

    @Override
    public void setNext(IEventConsumer nextConsumer) {
        this.nextConsumer = nextConsumer;
    }

    @Override
    public Event consume(Event event) {
        int id = event.getEventId();
        if (handledEventCache.putIfAbsent(id, "") == null) {
            nextConsumer.consume(event);
        }
        return event;
    }

}
