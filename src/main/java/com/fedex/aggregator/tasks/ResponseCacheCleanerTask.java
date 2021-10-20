package com.fedex.aggregator.tasks;

import com.fedex.aggregator.queues.subscribers.PricesResponseSubscriber;
import com.fedex.aggregator.queues.subscribers.ShipmentsResponseSubscriber;
import com.fedex.aggregator.queues.subscribers.TrackingResponseSubscriber;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

@Component
public class ResponseCacheCleanerTask {
    private static final Logger logger = LoggerFactory.getLogger(ResponseCacheCleanerTask.class);

    private static final int CACHE_TIMEOUT_IN_MILLIS = 1000;
    private static final int LAST_UPDATED_TIMEOUT_IN_MILLIS = 3000;

    @Scheduled(fixedRate = 1000)
    public void reportCurrentTime() {
        cleanUpCaches();
    }

    private void cleanUpCaches() {
        Long currentTimeInMillis = System.currentTimeMillis();
        if (PricesResponseSubscriber.prices.keySet().removeIf(
                key -> currentTimeInMillis - PricesResponseSubscriber.lastUpdated.get(key) > CACHE_TIMEOUT_IN_MILLIS
        )) {
            logger.info("Prices response cache cleaned up.");
        }
        PricesResponseSubscriber.lastUpdated.values().removeIf(value -> currentTimeInMillis - value > LAST_UPDATED_TIMEOUT_IN_MILLIS);

        if (ShipmentsResponseSubscriber.shipments.keySet().removeIf(
                key -> currentTimeInMillis - ShipmentsResponseSubscriber.lastUpdated.get(key) > CACHE_TIMEOUT_IN_MILLIS
        )) {
            logger.info("Shipments response cache cleaned up.");
        }
        ShipmentsResponseSubscriber.lastUpdated.values().removeIf(value -> currentTimeInMillis - value > LAST_UPDATED_TIMEOUT_IN_MILLIS);

        if (TrackingResponseSubscriber.trackingStatuses.keySet().removeIf(
                key -> currentTimeInMillis - TrackingResponseSubscriber.lastUpdated.get(key) > CACHE_TIMEOUT_IN_MILLIS
        )) {
            logger.info("Tracking response cache cleaned up.");
        }
        TrackingResponseSubscriber.lastUpdated.values().removeIf(value -> currentTimeInMillis - value > LAST_UPDATED_TIMEOUT_IN_MILLIS);
    }
}
