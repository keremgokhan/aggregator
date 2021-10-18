package com.fedex.aggregator.queues;

import org.springframework.data.redis.core.SetOperations;

import java.util.List;
import java.util.Set;

public class RequestsQueue {
    private SetOperations<String, Object> setOperations;

    public static final int MAX_REQUEST_QUEUE_SIZE = 5;

    public enum name {
        PRICING_REQUESTS("pricing"),
        TRACK_REQUESTS("track"),
        SHIPMENTS_REQUESTS("shipments");

        name(String name) {
        }
    }

    public RequestsQueue() {
    }

    public RequestsQueue(SetOperations<String, Object> setOperations) {
        this.setOperations = setOperations;
    }

    public void storeRequest(name queueName, String id) {
        this.setOperations.add(queueName.toString(), id);
    }

    public void storeRequests(name queueName, String...ids) {
        this.setOperations.add(queueName.toString(), ids);
    }

    public int getQueueSize(name queueName) {
        Long size = this.setOperations.size(queueName.toString());
        return size == null ? 0 : size.intValue();
    }

    public Set<Object> getCurrentItems(name queueName) {
        return this.setOperations.members(queueName.toString());
    }

    public List<Object> removeItems(name queueName, int count) {
        return this.setOperations.pop(queueName.toString(), count);
    }
}
