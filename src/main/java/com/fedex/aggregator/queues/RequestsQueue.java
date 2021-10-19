package com.fedex.aggregator.queues;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.data.redis.core.SetOperations;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Set;

public class RequestsQueue {
    private final Logger logger = LoggerFactory.getLogger(RequestsQueue.class);

    public static final int MAX_REQUEST_QUEUE_SIZE = 5;
    private final SetOperations<String, String> setOperations;

    public RequestsQueue(SetOperations<String, String> setOperations) {
        this.setOperations = setOperations;
    }

    public static HashMap<String, Long> lastUpdated = new HashMap<>();

    public Long getLastUpdated(name queueName) {
        return lastUpdated.get(queueName.value);
    }

    public void clearLastUpdated(name queueName) {
        lastUpdated.put(queueName.value, null);
    }

    public void storeRequest(name queueName, String id) {
        logger.info("Queue name: " + queueName.value + ", value to push: " + id);
        this.setOperations.add(queueName.value, id);
        lastUpdated.put(queueName.value, System.currentTimeMillis());
    }

    public void storeRequests(name queueName, String... ids) {
        this.setOperations.add(queueName.value, ids);
    }

    public int getQueueSize(name queueName) {
        Long size = this.setOperations.size(queueName.value);
        return size == null ? 0 : size.intValue();
    }

    public Set<String> getCurrentItems(name queueName) {
        return this.setOperations.members(queueName.value);
    }

    public List<String> removeItems(name queueName, int count) {
        return this.setOperations.pop(queueName.value, count);
    }

    public List<String> removeAllItems(name queueName) {
        Long size = this.setOperations.size(queueName.value);
        if (size == null) {
            return new ArrayList<>();
        }
        return this.setOperations.pop(queueName.value, size);
    }

    public enum name {
        PRICING_REQUESTS("qpricing"),
        TRACK_REQUESTS("qtrack"),
        SHIPMENTS_REQUESTS("qshipments");

        public final String value;

        name(String value) {
            this.value = value;
        }
    }
}
