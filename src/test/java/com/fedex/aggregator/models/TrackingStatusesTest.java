package com.fedex.aggregator.models;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.Test;
import org.springframework.core.io.ClassPathResource;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;

import static org.junit.jupiter.api.Assertions.assertEquals;

class TrackingStatusesTest {
    private final ObjectMapper objectMapper = new ObjectMapper();

    @Test
    public void givenSampleValues_whenDeserialized_thenInitializeTheObject() throws IOException {
        TrackingStatuses trackingStatuses = objectMapper.readValue(new ClassPathResource("models/trackingStatusesSample.json").getFile(), TrackingStatuses.class);
        TrackingStatuses trackingStatusesExpected = new TrackingStatuses();
        trackingStatusesExpected.put("109347263", null);
        trackingStatusesExpected.put("123456891", "COLLECTING");

        assertEquals(trackingStatusesExpected, trackingStatuses);
    }
}