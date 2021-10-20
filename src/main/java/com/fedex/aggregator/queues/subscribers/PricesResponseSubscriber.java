package com.fedex.aggregator.queues.subscribers;

import com.fedex.aggregator.models.Prices;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.data.redis.connection.Message;
import org.springframework.data.redis.connection.MessageListener;
import org.springframework.data.redis.serializer.GenericJackson2JsonRedisSerializer;
import org.springframework.stereotype.Service;

import java.util.HashMap;
import java.util.Map;

@Service
public class PricesResponseSubscriber implements MessageListener {
    public static Prices prices = new Prices();
    public static Map<String, Long> lastUpdated = new HashMap<>();
    private final Logger logger = LoggerFactory.getLogger(PricesResponseSubscriber.class);
    private final GenericJackson2JsonRedisSerializer serializer;

    public PricesResponseSubscriber(GenericJackson2JsonRedisSerializer serializer) {
        this.serializer = serializer;
    }

    public void onMessage(Message message, byte[] pattern) {
        Prices receivedPrices = this.deserializeMessage(message);
        for (String id : receivedPrices.keySet()) {
            lastUpdated.put(id, System.currentTimeMillis());
        }
        prices.putAll(receivedPrices);
        this.logger.info("Price received: " + receivedPrices);
    }

    private Prices deserializeMessage(Message message) {
        return (Prices) this.serializer.deserialize(message.getBody());
    }
}
