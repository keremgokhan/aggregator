package com.fedex.aggregator.aggregators;

import com.fedex.aggregator.clients.ApiClient;
import com.fedex.aggregator.models.AggregatedResults;
import com.fedex.aggregator.models.Prices;
import com.fedex.aggregator.models.Shipments;
import com.fedex.aggregator.models.TrackingStatuses;
import reactor.core.publisher.Mono;

import java.util.Arrays;
import java.util.List;

public class SimpleAggregator implements Aggregator {
    private final ApiClient apiClient;

    public SimpleAggregator(ApiClient apiClient) {
        this.apiClient = apiClient;
    }

    private List<String> removeDuplicates(String[] keys) {
        return List.of((String[]) Arrays.stream(keys).distinct().toArray());
    }

    public AggregatedResults aggregate(String[] pricing, String[] track, String[] shipments) {
        Mono<Prices> pricesMono = this.apiClient.getPricesMono(removeDuplicates(pricing));
        Mono<TrackingStatuses> trackingStatusesMono = this.apiClient.getTrackingStatusesMono(removeDuplicates(track));
        Mono<Shipments> shipmentsMono = this.apiClient.getShipmentsMono(removeDuplicates(shipments));
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
