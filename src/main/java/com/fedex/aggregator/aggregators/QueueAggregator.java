package com.fedex.aggregator.aggregators;

import com.fedex.aggregator.clients.ApiClient;
import com.fedex.aggregator.models.AggregatedResults;
import com.fedex.aggregator.models.Prices;
import com.fedex.aggregator.queues.RequestsQueue;
import org.springframework.web.reactive.function.client.WebClient;
import reactor.core.publisher.Mono;

import java.util.List;
import java.util.Set;

public class QueueAggregator {
    private final RequestsQueue pricingRequestsQueue;
    private final ApiClient apiClient;

    public QueueAggregator(RequestsQueue pricingRequestsQueue, ApiClient apiClient) {
        this.pricingRequestsQueue = pricingRequestsQueue;
        this.apiClient = apiClient;
    }

    public AggregatedResults aggregate(String[] pricing, String[] track, String[] shipments) {
        Set<String> countryCodesSet = Set.of(pricing);
        if(countryCodesSet.size() > 0) {
            for(String cc : countryCodesSet) {
                if (this.pricingRequestsQueue.getQueueSize() >= 5) {
                    List<String> ccsToRequest = this.pricingRequestsQueue.removeItems(5);
                    Mono<Prices> pricesMono = this.apiClient.getPricesMono(ccsToRequest);
                }
                this.pricingRequestsQueue.storeRequest(cc);
            }
        }
        Set<String> trackingIdsSet = Set.of(track);
        Set<String> shipmentIdsSet = Set.of(shipments);


    }
}
