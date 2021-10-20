package com.fedex.aggregator.controllers;

import com.fedex.aggregator.aggregators.Aggregator;
import com.fedex.aggregator.aggregators.QueueAggregator;
import com.fedex.aggregator.aggregators.SimpleAggregator;
import com.fedex.aggregator.clients.ApiClient;
import com.fedex.aggregator.models.AggregatedResults;
import com.fedex.aggregator.queues.RequestsQueue;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.data.redis.core.ValueOperations;
import org.springframework.data.redis.core.ZSetOperations;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import javax.annotation.PostConstruct;
import javax.annotation.Resource;

@RestController
public class AggregationController {
    private ApiClient apiClient;
    private RequestsQueue requestsQueue;
    private Aggregator aggregator;
    private Aggregator simpleAggregator;

    @Resource(name = "redisTemplate")
    private ZSetOperations<String, String> setOperations;

    @Resource(name = "redisTemplate")
    private ValueOperations<String, String> valueOperations;

    @Resource(name = "redisTemplate")
    private RedisTemplate<String, Object> publisherOperations;

    @PostConstruct
    public void init() {
        this.setApiClient(new ApiClient());
        this.setRequestsQueue(new RequestsQueue(setOperations));
        this.setAggregator(new QueueAggregator(this.getRequestsQueue(), this.getApiClient(), publisherOperations));
        this.setSimpleAggregator(new SimpleAggregator(this.getApiClient()));
    }

    public ApiClient getApiClient() {
        return apiClient;
    }

    public void setApiClient(ApiClient apiClient) {
        this.apiClient = apiClient;
    }

    public Aggregator getAggregator() {
        return aggregator;
    }

    public void setAggregator(Aggregator aggregator) {
        this.aggregator = aggregator;
    }

    public Aggregator getSimpleAggregator() {
        return simpleAggregator;
    }

    public void setSimpleAggregator(Aggregator simpleAggregator) {
        this.simpleAggregator = simpleAggregator;
    }

    public RequestsQueue getRequestsQueue() {
        return requestsQueue;
    }

    public void setRequestsQueue(RequestsQueue requestsQueue) {
        this.requestsQueue = requestsQueue;
    }

    @GetMapping("/aggregation")
    AggregatedResults aggregation(@RequestParam String[] pricing, @RequestParam String[] track, @RequestParam String[] shipments) {
        return this.getAggregator().aggregate(pricing, track, shipments);
    }

    @GetMapping("/aggregation/simple")
    AggregatedResults simpleAggregation(@RequestParam String[] pricing, @RequestParam String[] track, @RequestParam String[] shipments) {
        return this.getSimpleAggregator().aggregate(pricing, track, shipments);
    }
}
