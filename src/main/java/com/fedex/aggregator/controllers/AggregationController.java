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

    @PostConstruct
    public void init() {
        this.apiClient = new ApiClient();
    }

    @GetMapping("/aggregation")
    AggregatedResults aggregation(@RequestParam String[] pricing, @RequestParam String[] track, @RequestParam String[] shipments) {
        SimpleAggregator aggregator = new SimpleAggregator(this.apiClient);
        return aggregator.aggregate(pricing, track, shipments);
    }
}
