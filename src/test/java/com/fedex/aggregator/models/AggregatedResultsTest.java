package com.fedex.aggregator.models;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.Test;
import org.springframework.core.io.ClassPathResource;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;

import static org.junit.jupiter.api.Assertions.*;

class AggregatedResultsTest {
    private final ObjectMapper objectMapper = new ObjectMapper();

    @Test
    public void givenSampleValues_whenDeserialized_thenInitializeTheObject() throws IOException {
        AggregatedResults aggregatedResults = objectMapper.readValue(new ClassPathResource("models/aggregatedResultsSample.json").getFile(), AggregatedResults.class);
        AggregatedResults aggregatedResultsExpected = new AggregatedResults();
        Prices prices = new Prices();
        prices.put("NL", 14.242090605778);
        prices.put("CN", 20.503467806384);
        aggregatedResultsExpected.setPricing(prices);
        TrackingStatuses trackingStatuses = new TrackingStatuses();
        trackingStatuses.put("109347263", null);
        trackingStatuses.put("123456891", "COLLECTING");
        aggregatedResultsExpected.setTrack(trackingStatuses);
        Shipments shipments = new Shipments();
        shipments.put("109347263", new ArrayList<String>(Arrays.asList("box", "box", "pallet")));
        shipments.put("123456891", null);
        aggregatedResultsExpected.setShipments(shipments);

        assertEquals(aggregatedResultsExpected, aggregatedResults);
    }
}