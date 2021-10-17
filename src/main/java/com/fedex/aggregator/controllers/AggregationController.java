package com.fedex.aggregator.controllers;

import com.fedex.aggregator.aggregators.SimpleAggregator;
import com.fedex.aggregator.clients.ApiClient;
import com.fedex.aggregator.models.AggregatedResults;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import javax.annotation.PostConstruct;

@RestController
public class AggregationController {
    private ApiClient apiClient;
    private SimpleAggregator aggregator;

    @PostConstruct
    public void init() {
        this.setApiClient(new ApiClient());
        this.setAggregator(new SimpleAggregator(this.getApiClient()));
    }

    public ApiClient getApiClient() {
        return apiClient;
    }

    public void setApiClient(ApiClient apiClient) {
        this.apiClient = apiClient;
    }

    public SimpleAggregator getAggregator() {
        return aggregator;
    }

    public void setAggregator(SimpleAggregator aggregator) {
        this.aggregator = aggregator;
    }

    @GetMapping("/aggregation")
    AggregatedResults aggregation(@RequestParam String[] pricing, @RequestParam String[] track, @RequestParam String[] shipments) {
        return this.getAggregator().aggregate(pricing, track, shipments);
    }
}
