package com.fedex.aggregator.tasks;

import com.fedex.aggregator.aggregators.QueueAggregator;
import com.fedex.aggregator.clients.ApiClient;
import com.fedex.aggregator.queues.RequestsQueue;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.data.redis.core.ValueOperations;
import org.springframework.data.redis.core.ZSetOperations;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

import javax.annotation.PostConstruct;
import javax.annotation.Resource;
import java.util.List;

@Component
public class RequestCacheTimeoutTask {
    private static final Logger logger = LoggerFactory.getLogger(RequestCacheTimeoutTask.class);

    private static final int CACHE_TIMEOUT_IN_MILLIS = 5000;
    private static final int LAST_UPDATED_TIMEOUT_IN_MILLIS = 10000;

    private RequestsQueue requestsQueue;
    private ApiClient apiClient;
    private QueueAggregator queueAggregator;

    @Resource(name = "redisTemplate")
    private ZSetOperations<String, String> setOperations;

    @Resource(name = "redisTemplate")
    private RedisTemplate<String, Object> publisherOperations;

    @Resource(name = "redisTemplate")
    private ValueOperations<String, String> valueOperations;

    public RequestCacheTimeoutTask() {
    }

    public RequestCacheTimeoutTask(RequestsQueue requestsQueue, ApiClient apiClient) {
        this.requestsQueue = requestsQueue;
        this.apiClient = apiClient;
    }

    @PostConstruct
    public void init() {
        this.apiClient = new ApiClient();
        this.requestsQueue = new RequestsQueue(setOperations);
        this.queueAggregator = new QueueAggregator(this.requestsQueue, this.apiClient, publisherOperations);
    }

    @Scheduled(fixedRate = 500)
    public void timeoutOldQueues() {
        cleanUpCaches(RequestsQueue.name.PRICING_REQUESTS);
        cleanUpCaches(RequestsQueue.name.TRACK_REQUESTS);
        cleanUpCaches(RequestsQueue.name.SHIPMENTS_REQUESTS);
    }

    private void cleanUpCaches(RequestsQueue.name queueName) {
        new Thread(() -> {
            List<String> removedRequests = this.requestsQueue.removeTimedOutItems(queueName);
            if (removedRequests != null && removedRequests.size() > 0) {
                this.queueAggregator.makeRequestAndPublishResponse(queueName, removedRequests);
            }
        }).start();
    }
}
