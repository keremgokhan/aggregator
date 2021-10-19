package com.fedex.aggregator.queues.publishers;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.data.redis.core.RedisTemplate;

public class ResponsePublisher<T> {
    private final Logger logger = LoggerFactory.getLogger(ResponsePublisher.class);

    private final topic messageTopic;
    private final RedisTemplate<String, Object> redisTemplate;

    public ResponsePublisher(RedisTemplate<String, Object> redisTemplate, topic messageTopic) {
        this.redisTemplate = redisTemplate;
        this.messageTopic = messageTopic;
    }

    public void publish(T response) {
        this.redisTemplate.convertAndSend(this.messageTopic.value, response);
        this.logger.info("Published message " + response);
    }

    public enum topic {
        PRICING_RESPONSES("pricing/responses"),
        TRACK_RESPONSES("track/responses"),
        SHIPMENTS_RESPONSES("shipments/responses");

        public final String value;

        topic(String value) {
            this.value = value;
        }
    }

}
