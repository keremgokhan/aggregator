package com.fedex.aggregator.models;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.core.io.ClassPathResource;

import java.io.IOException;

import static org.junit.jupiter.api.Assertions.assertEquals;

@SpringBootTest
class TrackingStatusesTest {
    @Autowired
    private ObjectMapper objectMapper;

    @Test
    public void givenSampleValues_whenDeserialized_thenInitializeTheObject() throws IOException {
        TrackingStatuses trackingStatuses = objectMapper.readValue(new ClassPathResource("models/trackingStatusesSample.json").getFile(), TrackingStatuses.class);
        TrackingStatuses trackingStatusesExpected = new TrackingStatuses();
        trackingStatusesExpected.put("109347263", null);
        trackingStatusesExpected.put("123456891", "COLLECTING");

        assertEquals(trackingStatusesExpected, trackingStatuses);
    }
}