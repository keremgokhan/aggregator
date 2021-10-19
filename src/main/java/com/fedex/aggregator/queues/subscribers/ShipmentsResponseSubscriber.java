package com.fedex.aggregator.queues.subscribers;

import com.fedex.aggregator.models.Shipments;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.data.redis.connection.Message;
import org.springframework.data.redis.connection.MessageListener;
import org.springframework.data.redis.serializer.GenericJackson2JsonRedisSerializer;
import org.springframework.stereotype.Service;

import java.util.HashMap;
import java.util.Map;

@Service
public class ShipmentsResponseSubscriber implements MessageListener {
    private final Logger logger = LoggerFactory.getLogger(ShipmentsResponseSubscriber.class);

    private final GenericJackson2JsonRedisSerializer serializer;

    public static Shipments shipments = new Shipments();
    public static Map<String, Long> lastUpdated = new HashMap<>();

    public ShipmentsResponseSubscriber(GenericJackson2JsonRedisSerializer serializer) {
        this.serializer = serializer;
    }

    public void onMessage(Message message, byte[] pattern) {
        Shipments receivedShipments = this.deserializeMessage(message);
        for (String id : receivedShipments.keySet()) {
            lastUpdated.put(id, System.currentTimeMillis());
        }
        shipments.putAll(receivedShipments);
        this.logger.info("Message received: " + receivedShipments);
    }

    private Shipments deserializeMessage(Message message) {
        return (Shipments) this.serializer.deserialize(message.getBody());
    }
}
