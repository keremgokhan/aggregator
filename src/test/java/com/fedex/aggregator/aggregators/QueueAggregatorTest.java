package com.fedex.aggregator.aggregators;

import com.fedex.aggregator.clients.ApiClient;
import com.fedex.aggregator.models.Prices;
import com.fedex.aggregator.models.Shipments;
import com.fedex.aggregator.models.TrackingStatuses;
import com.fedex.aggregator.queues.RequestsQueue;
import com.fedex.aggregator.queues.publishers.ResponsePublisher;
import org.junit.jupiter.api.Test;
import org.springframework.data.redis.core.RedisTemplate;
import reactor.core.publisher.Mono;

import java.util.List;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.*;

class QueueAggregatorTest {

    @Test
    void givenRequests_whenMakeRequestAndPublishResponse_MakeRequestsAndPublishResponses() {
        RequestsQueue mockedRequestQueue = mock(RequestsQueue.class);
        doNothing().when(mockedRequestQueue).storeRequest(any(), anyString());

        ApiClient mockedApiClient = mock(ApiClient.class);
        Mono<Prices> pricesMono = mock(Mono.class);
        when(pricesMono.block()).thenReturn(new Prices());
        when(mockedApiClient.getPricesMono(any())).thenReturn(pricesMono);
        Mono<TrackingStatuses> trackingStatusesMono = mock(Mono.class);
        when(trackingStatusesMono.block()).thenReturn(new TrackingStatuses());
        when(mockedApiClient.getTrackingStatusesMono(any())).thenReturn(trackingStatusesMono);
        Mono<Shipments> shipmentsMono = mock(Mono.class);
        when(shipmentsMono.block()).thenReturn(new Shipments());
        when(mockedApiClient.getShipmentsMono(any())).thenReturn(shipmentsMono);

        RedisTemplate<String, Object> mockedRedisTemplate = mock(RedisTemplate.class);
        doNothing().when(mockedRedisTemplate).convertAndSend(anyString(), any());

        QueueAggregator queueAggregator = new QueueAggregator(mockedRequestQueue, mockedApiClient, mockedRedisTemplate);

        queueAggregator.makeRequestAndPublishResponse(RequestsQueue.name.PRICING_REQUESTS, List.of("NL", "CN"));
        verify(mockedApiClient).getPricesMono(eq(List.of("NL", "CN")));
        verify(pricesMono).block();
        verify(mockedRedisTemplate).convertAndSend(eq(ResponsePublisher.topic.PRICING_RESPONSES.value), any(Prices.class));

        queueAggregator.makeRequestAndPublishResponse(RequestsQueue.name.TRACK_REQUESTS, List.of("109347263", "123456891"));
        verify(mockedApiClient).getTrackingStatusesMono(eq(List.of("109347263", "123456891")));
        verify(trackingStatusesMono).block();
        verify(mockedRedisTemplate).convertAndSend(eq(ResponsePublisher.topic.TRACK_RESPONSES.value), any(TrackingStatuses.class));

        queueAggregator.makeRequestAndPublishResponse(RequestsQueue.name.SHIPMENTS_REQUESTS, List.of("109347263", "123456891"));
        verify(mockedApiClient).getShipmentsMono(eq(List.of("109347263", "123456891")));
        verify(shipmentsMono).block();
        verify(mockedRedisTemplate).convertAndSend(eq(ResponsePublisher.topic.SHIPMENTS_RESPONSES.value), any(Shipments.class));

    }
}