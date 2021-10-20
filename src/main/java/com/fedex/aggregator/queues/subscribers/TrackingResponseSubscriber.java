package com.fedex.aggregator.queues.subscribers;

import com.fedex.aggregator.models.TrackingStatuses;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.data.redis.connection.Message;
import org.springframework.data.redis.connection.MessageListener;
import org.springframework.data.redis.serializer.GenericJackson2JsonRedisSerializer;
import org.springframework.stereotype.Service;

import java.util.HashMap;
import java.util.Map;

@Service
public class TrackingResponseSubscriber implements MessageListener {
    public static TrackingStatuses trackingStatuses = new TrackingStatuses();
    public static Map<String, Long> lastUpdated = new HashMap<>();
    private final Logger logger = LoggerFactory.getLogger(TrackingResponseSubscriber.class);
    private final GenericJackson2JsonRedisSerializer serializer;

    public TrackingResponseSubscriber(GenericJackson2JsonRedisSerializer serializer) {
        this.serializer = serializer;
    }

    public void onMessage(Message message, byte[] pattern) {
        TrackingStatuses receivedTracking = this.deserializeMessage(message);
        for (String id : receivedTracking.keySet()) {
            lastUpdated.put(id, System.currentTimeMillis());
        }
        trackingStatuses.putAll(receivedTracking);
        this.logger.info("Message received: " + receivedTracking);
    }

    private TrackingStatuses deserializeMessage(Message message) {
        return (TrackingStatuses) this.serializer.deserialize(message.getBody());
    }
}
