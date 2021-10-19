package com.fedex.aggregator.queues;

import org.springframework.data.redis.core.SetOperations;

import java.util.List;
import java.util.Set;

public class RequestsQueue {
    public static final int MAX_REQUEST_QUEUE_SIZE = 5;
    private final SetOperations<String, String> setOperations;

    public RequestsQueue(SetOperations<String, String> setOperations) {
        this.setOperations = setOperations;
    }

    public void storeRequest(name queueName, String id) {
        this.setOperations.add(queueName.value, id);
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

    public enum name {
        PRICING_REQUESTS("pricing"),
        TRACK_REQUESTS("track"),
        SHIPMENTS_REQUESTS("shipments");

        public final String value;

        name(String value) {
            this.value = value;
        }
    }
}
