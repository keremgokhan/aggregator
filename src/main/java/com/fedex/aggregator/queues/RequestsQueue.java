package com.fedex.aggregator.queues;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.data.redis.core.ZSetOperations;

import java.util.ArrayList;
import java.util.List;
import java.util.Set;

public class RequestsQueue {
    public static final int MAX_REQUEST_QUEUE_SIZE = 5;
    public final static int TIMEOUT_IN_MILLIS = 5000;
    private final Logger logger = LoggerFactory.getLogger(RequestsQueue.class);
    private final ZSetOperations<String, String> setOperations;

    public RequestsQueue(ZSetOperations<String, String> setOperations) {
        this.setOperations = setOperations;
    }

    public void storeRequest(name queueName, String id) {
        if (this.setOperations.score(queueName.toString(), id) == null) {
            this.setOperations.add(queueName.toString(), id, System.currentTimeMillis());
            logger.info("Queue name: " + queueName + ", request to push: " + id);
        }
    }

    public int getQueueSize(name queueName) {
        Long size = this.setOperations.size(queueName.toString());
        return size == null ? 0 : size.intValue();
    }

    public List<String> removeItems(name queueName, int count) {
        Set<String> itemsToRemove = this.setOperations.range(queueName.toString(), 0, count);
        if (itemsToRemove == null || itemsToRemove.size() == 0) {
            return new ArrayList<>();
        }
        Long removedCount = this.setOperations.removeRange(queueName.toString(), 0, count);
        if (removedCount == null) {
            return new ArrayList<>();
        }
        logger.info("Queue name: " + queueName + ", removed items: " + removedCount);
        return new ArrayList<>(itemsToRemove);
    }

    public List<String> removeAllItems(name queueName) {
        return this.removeItems(queueName, MAX_REQUEST_QUEUE_SIZE);
    }

    public List<String> removeTimedOutItems(name queueName) {
        long endInMillis = System.currentTimeMillis() - TIMEOUT_IN_MILLIS;
        long startInMillis = endInMillis - 6000000;
        Set<String> outdated = this.setOperations.rangeByScore(queueName.toString(), startInMillis, endInMillis);
        if (outdated == null || outdated.size() == 0) {
            return new ArrayList<>();
        }
        logger.info("Queue name: " + queueName + ", queue timeout");
        return this.removeAllItems(queueName);
    }

    public enum name {
        PRICING_REQUESTS("qpricing"),
        TRACK_REQUESTS("qtrack"),
        SHIPMENTS_REQUESTS("qshipments");

        public final String value;

        name(String value) {
            this.value = value;
        }

        @Override
        public String toString() {
            return value;
        }
    }
}
