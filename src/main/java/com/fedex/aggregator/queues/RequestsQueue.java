package com.fedex.aggregator.queues;

import org.springframework.data.redis.core.SetOperations;

import javax.annotation.Resource;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

public class RequestsQueue {

    @Resource(name="redisTemplate")
    private final SetOperations<String,String> setOperations;
    private final String queueName;
    public static final int MAX_REQUEST_QUEUE_SIZE = 5;

    public RequestsQueue(SetOperations<String, String> setOperations, String queueName) {
        this.setOperations = setOperations;
        this.queueName = queueName;
    }

    public void storeRequest(String countryCode) {
        this.setOperations.add(this.queueName, countryCode);
    }

    public void storeRequests(String...countryCodes) {
        this.setOperations.add(this.queueName, countryCodes);
    }

    public int getQueueSize() {
        Long size = this.setOperations.size(this.queueName);
        return size == null ? 0 : size.intValue();
    }

    public Set<String> getCurrentItems() {
        return this.setOperations.members(this.queueName);
    }

    public List<String> removeItems(int count) {
        return this.setOperations.pop(this.queueName, count);
    }
}
