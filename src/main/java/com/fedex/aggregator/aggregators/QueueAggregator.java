package com.fedex.aggregator.aggregators;

import com.fedex.aggregator.clients.ApiClient;
import com.fedex.aggregator.models.AggregatedResults;
import com.fedex.aggregator.models.Prices;
import com.fedex.aggregator.models.Shipments;
import com.fedex.aggregator.models.TrackingStatuses;
import com.fedex.aggregator.queues.RequestsQueue;
import com.fedex.aggregator.queues.ResponsePublisher;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.data.redis.core.RedisTemplate;
import reactor.core.publisher.Mono;

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
                List<String> idsToRequest = this.requestsQueue.removeItems(queueName, RequestsQueue.MAX_REQUEST_QUEUE_SIZE);
                logger.info("Queue size after removing items from " + queueName + ": " + this.requestsQueue.getQueueSize(queueName));
                Thread newThread = new Thread(() -> {
                    makeRequestAndPublishResponse(queueName, idsToRequest);
                });
                newThread.start();
            }
        }
    }

    private void makeRequestAndPublishResponse(RequestsQueue.name queueName, List<String> ids) {
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
        ResponsePublisher<T> responsePublisher = new ResponsePublisher<T>(this.redisTemplate, messageTopic);
        responsePublisher.publish(response);
    }

    public AggregatedResults aggregate(String[] pricing, String[] track, String[] shipments) {
        new Thread(() -> {
            aggregateRequests(RequestsQueue.name.PRICING_REQUESTS, pricing);
        }).start();
        new Thread(() -> {
            aggregateRequests(RequestsQueue.name.TRACK_REQUESTS, track);
        }).start();
        new Thread(() -> {
            aggregateRequests(RequestsQueue.name.SHIPMENTS_REQUESTS, shipments);
        }).start();

        return new AggregatedResults();
    }
}
