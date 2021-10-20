package com.fedex.aggregator.aggregators;

import com.fedex.aggregator.clients.ApiClient;
import com.fedex.aggregator.models.AggregatedResults;
import com.fedex.aggregator.models.Prices;
import com.fedex.aggregator.models.Shipments;
import com.fedex.aggregator.models.TrackingStatuses;
import com.fedex.aggregator.queues.RequestsQueue;
import com.fedex.aggregator.queues.publishers.ResponsePublisher;
import com.fedex.aggregator.queues.subscribers.PricesResponseSubscriber;
import com.fedex.aggregator.queues.subscribers.ShipmentsResponseSubscriber;
import com.fedex.aggregator.queues.subscribers.TrackingResponseSubscriber;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.data.redis.core.RedisTemplate;
import reactor.core.publisher.Mono;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

public class QueueAggregator implements Aggregator {
    private final Logger logger = LoggerFactory.getLogger(QueueAggregator.class);

    private final RequestsQueue requestsQueue;
    private final ApiClient apiClient;
    private final RedisTemplate<String, Object> redisTemplate;

    public QueueAggregator(RequestsQueue requestsQueue, ApiClient apiClient, RedisTemplate<String, Object> redisTemplate) {
        this.requestsQueue = requestsQueue;
        this.apiClient = apiClient;
        this.redisTemplate = redisTemplate;
    }

    private void aggregateRequests(RequestsQueue.name queueName, String[] idsArray) {
        Set<String> ids = Set.of(idsArray);
        if (ids.size() == 0) {
            return;
        }

        for (String id : ids) {
            this.requestsQueue.storeRequest(queueName, id);
            logger.info("Queue size after push to " + queueName + ": " + this.requestsQueue.getQueueSize(queueName));

            if (this.requestsQueue.getQueueSize(queueName) >= RequestsQueue.MAX_REQUEST_QUEUE_SIZE) {
                List<String> idsToRequest = this.requestsQueue.removeAllItems(queueName);
                logger.info("Queue size after removing items from " + queueName + ": " + this.requestsQueue.getQueueSize(queueName));
                new Thread(() -> makeRequestAndPublishResponse(queueName, idsToRequest)).start();
            }
        }
    }

    public void makeRequestAndPublishResponse(RequestsQueue.name queueName, List<String> ids) {
        if (queueName == RequestsQueue.name.PRICING_REQUESTS) {
            Mono<Prices> pricesMono = this.apiClient.getPricesMono(ids);
            blockMonoAndPublishResponse(ResponsePublisher.topic.PRICING_RESPONSES, pricesMono);
        } else if (queueName == RequestsQueue.name.TRACK_REQUESTS) {
            Mono<TrackingStatuses> trackingStatusesMono = this.apiClient.getTrackingStatusesMono(ids);
            blockMonoAndPublishResponse(ResponsePublisher.topic.TRACK_RESPONSES, trackingStatusesMono);
        } else if (queueName == RequestsQueue.name.SHIPMENTS_REQUESTS) {
            Mono<Shipments> shipmentsMono = this.apiClient.getShipmentsMono(ids);
            blockMonoAndPublishResponse(ResponsePublisher.topic.SHIPMENTS_RESPONSES, shipmentsMono);
        }
    }

    private <T> void blockMonoAndPublishResponse(ResponsePublisher.topic messageTopic, Mono<T> requestMono) {
        T response = requestMono.block();
        ResponsePublisher<T> responsePublisher = new ResponsePublisher<>(this.redisTemplate, messageTopic);
        responsePublisher.publish(response);
    }

    @SuppressWarnings("BusyWait")
    public AggregatedResults aggregate(String[] pricing, String[] track, String[] shipments) {
        new Thread(() -> aggregateRequests(RequestsQueue.name.PRICING_REQUESTS, pricing)).start();
        new Thread(() -> aggregateRequests(RequestsQueue.name.TRACK_REQUESTS, track)).start();
        new Thread(() -> aggregateRequests(RequestsQueue.name.SHIPMENTS_REQUESTS, shipments)).start();

        AggregatedResults aggregatedResults = new AggregatedResults();
        aggregatedResults.setPricing(new Prices());
        aggregatedResults.setShipments(new Shipments());
        aggregatedResults.setTrack(new TrackingStatuses());

        Set<String> pricingRequests = new HashSet<>(Set.of(pricing));
        Thread pricingResultsThread = new Thread(() -> {
            while (!pricingRequests.isEmpty()) {
                try {
                    List<String> toRemove = new ArrayList<>();
                    for (String cc : pricingRequests) {
                        if (PricesResponseSubscriber.prices.containsKey(cc)) {
                            toRemove.add(cc);
                            aggregatedResults.getPricing().put(cc, PricesResponseSubscriber.prices.get(cc));
                        }
                    }
                    toRemove.forEach(pricingRequests::remove);
                    Thread.sleep(50);
                } catch (InterruptedException e) {
                    e.printStackTrace();
                }
            }
        });

        Set<String> trackingRequests = new HashSet<>(Set.of(track));
        Thread trackingResultsThread = new Thread(() -> {
            while (!trackingRequests.isEmpty()) {
                try {
                    List<String> toRemove = new ArrayList<>();
                    for (String id : trackingRequests) {
                        if (TrackingResponseSubscriber.trackingStatuses.containsKey(id)) {
                            toRemove.add(id);
                            aggregatedResults.getTrack().put(id, TrackingResponseSubscriber.trackingStatuses.get(id));
                        }
                    }
                    toRemove.forEach(trackingRequests::remove);
                    Thread.sleep(50);
                } catch (InterruptedException e) {
                    e.printStackTrace();
                }
            }
        });

        Set<String> shipmentsRequests = new HashSet<>(Set.of(shipments));
        Thread shipmentsResultsThread = new Thread(() -> {
            while (!shipmentsRequests.isEmpty()) {
                try {
                    List<String> toRemove = new ArrayList<>();
                    for (String id : shipmentsRequests) {
                        if (ShipmentsResponseSubscriber.shipments.containsKey(id)) {
                            toRemove.add(id);
                            aggregatedResults.getShipments().put(id, ShipmentsResponseSubscriber.shipments.get(id));
                        }
                    }
                    toRemove.forEach(shipmentsRequests::remove);
                    Thread.sleep(50);
                } catch (InterruptedException e) {
                    e.printStackTrace();
                }
            }
        });

        pricingResultsThread.start();
        trackingResultsThread.start();
        shipmentsResultsThread.start();

        try {
            pricingResultsThread.join();
            trackingResultsThread.join();
            shipmentsResultsThread.join();
        } catch (InterruptedException e) {
            this.logger.error("Error processing response" + e);
        }

        return aggregatedResults;
    }
}
