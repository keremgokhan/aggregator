package com.fedex.aggregator;

import com.fedex.aggregator.queues.publishers.ResponsePublisher;
import com.fedex.aggregator.queues.subscribers.PricesResponseSubscriber;
import com.fedex.aggregator.queues.subscribers.ShipmentsResponseSubscriber;
import com.fedex.aggregator.queues.subscribers.TrackingResponseSubscriber;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.data.redis.connection.RedisConnectionFactory;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.data.redis.listener.ChannelTopic;
import org.springframework.data.redis.listener.RedisMessageListenerContainer;
import org.springframework.data.redis.listener.adapter.MessageListenerAdapter;
import org.springframework.data.redis.serializer.GenericJackson2JsonRedisSerializer;

@Configuration
public class AppConfig {

    @Bean(name = "jackson2JsonRedisSerializer")
    public GenericJackson2JsonRedisSerializer jackson2JsonRedisSerializer() {
        return new GenericJackson2JsonRedisSerializer();
    }

    @Bean(name = "redisTemplate")
    public RedisTemplate<String, Object> requestTemplate(RedisConnectionFactory redisConnectionFactory) {
        RedisTemplate<String, Object> template = new RedisTemplate<>();
        template.setConnectionFactory(redisConnectionFactory);
        template.setDefaultSerializer(jackson2JsonRedisSerializer());
        // Add some specific configuration here. Key serializers, etc.
        return template;
    }

    @Bean
    MessageListenerAdapter pricingResponseListener() {
        return new MessageListenerAdapter(new PricesResponseSubscriber(jackson2JsonRedisSerializer()));
    }

    @Bean
    RedisMessageListenerContainer pricingResponseContainer(RedisConnectionFactory redisConnectionFactory) {
        RedisMessageListenerContainer container
                = new RedisMessageListenerContainer();
        container.setConnectionFactory(redisConnectionFactory);
        container.addMessageListener(pricingResponseListener(), new ChannelTopic(ResponsePublisher.topic.PRICING_RESPONSES.value));
        return container;
    }

    @Bean
    MessageListenerAdapter trackingResponseListener() {
        return new MessageListenerAdapter(new TrackingResponseSubscriber(jackson2JsonRedisSerializer()));
    }

    @Bean
    RedisMessageListenerContainer trackingResponseContainer(RedisConnectionFactory redisConnectionFactory) {
        RedisMessageListenerContainer container
                = new RedisMessageListenerContainer();
        container.setConnectionFactory(redisConnectionFactory);
        container.addMessageListener(trackingResponseListener(), new ChannelTopic(ResponsePublisher.topic.TRACK_RESPONSES.value));
        return container;
    }

    @Bean
    MessageListenerAdapter shipmentsResponseListener() {
        return new MessageListenerAdapter(new ShipmentsResponseSubscriber(jackson2JsonRedisSerializer()));
    }

    @Bean
    RedisMessageListenerContainer shipmentsResponseContainer(RedisConnectionFactory redisConnectionFactory) {
        RedisMessageListenerContainer container
                = new RedisMessageListenerContainer();
        container.setConnectionFactory(redisConnectionFactory);
        container.addMessageListener(shipmentsResponseListener(), new ChannelTopic(ResponsePublisher.topic.SHIPMENTS_RESPONSES.value));
        return container;
    }

}
