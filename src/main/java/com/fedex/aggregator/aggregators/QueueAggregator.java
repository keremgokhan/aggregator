package com.fedex.aggregator.aggregators;

import com.fedex.aggregator.clients.ApiClient;
import com.fedex.aggregator.models.AggregatedResults;
import com.fedex.aggregator.queues.RequestsQueue;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.List;
import java.util.Set;

public class QueueAggregator implements Aggregator {
    private final Logger logger = LoggerFactory.getLogger(QueueAggregator.class);

    private final RequestsQueue requestsQueue;
    private final ApiClient apiClient;

    public QueueAggregator(RequestsQueue requestsQueue, ApiClient apiClient) {
        this.requestsQueue = requestsQueue;
        this.apiClient = apiClient;
    }

    private void aggregateRequests(RequestsQueue.name queueName, Set<String> ids) {
        int pushed = 0;
        int popped = 0;
        for(String id : ids) {
            if (this.requestsQueue.getQueueSize(queueName) >= 5) {
                List<Object> idsToRequest = this.requestsQueue.removeItems(queueName, 5);
                popped += idsToRequest.size();
            }
            this.requestsQueue.storeRequest(queueName, id);
            pushed++;
            logger.info("Queue size after push to " + queueName + ": " + this.requestsQueue.getQueueSize(queueName));
        }
        this.logger.info("Pushed " + pushed + ", popped " + popped + " ids to the " + queueName.toString() + " queue.");
    }

    public AggregatedResults aggregate(String[] pricing, String[] track, String[] shipments) {
        Set<String> countryCodesSet = Set.of(pricing);
        if(countryCodesSet.size() > 0) {
            aggregateRequests(RequestsQueue.name.PRICING_REQUESTS, countryCodesSet);
        }
        Set<String> trackingIdsSet = Set.of(track);
        if(trackingIdsSet.size() > 0) {
            aggregateRequests(RequestsQueue.name.TRACK_REQUESTS, trackingIdsSet);
        }
        Set<String> shipmentIdsSet = Set.of(shipments);
        if(shipmentIdsSet.size() > 0) {
            aggregateRequests(RequestsQueue.name.SHIPMENTS_REQUESTS, shipmentIdsSet);
        }

        return new AggregatedResults();
    }
}
