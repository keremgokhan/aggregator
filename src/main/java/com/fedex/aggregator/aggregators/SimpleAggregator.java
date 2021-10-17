package com.fedex.aggregator.aggregators;

import com.fedex.aggregator.clients.ApiClient;
import com.fedex.aggregator.models.AggregatedResults;
import com.fedex.aggregator.models.Prices;
import com.fedex.aggregator.models.Shipments;
import com.fedex.aggregator.models.TrackingStatuses;
import reactor.core.publisher.Mono;
import reactor.core.scheduler.Schedulers;

import java.util.List;

public class SimpleAggregator {
    private final ApiClient apiClient;

    public SimpleAggregator(ApiClient apiClient) {
        this.apiClient = apiClient;
    }

    public AggregatedResults aggregate(String[] pricing, String[] track, String[] shipments) {
        Mono<Prices> pricesMono = this.apiClient.get("/pricing", List.of(pricing), Prices.class);
        Mono<TrackingStatuses> trackingStatusesMono = this.apiClient.get("/track", List.of(track), TrackingStatuses.class);
        Mono<Shipments> shipmentsMono = this.apiClient.get("/shipments", List.of(shipments), Shipments.class);
        Mono<AggregatedResults> aggregatedResultsMono = Mono.zip(pricesMono, trackingStatusesMono, shipmentsMono).flatMap(data -> {
            AggregatedResults aggregatedResults = new AggregatedResults();
            aggregatedResults.setPricing(data.getT1());
            aggregatedResults.setTrack(data.getT2());
            aggregatedResults.setShipments(data.getT3());
            return Mono.just(aggregatedResults);
        });

        return aggregatedResultsMono.block();
    }
}
