package com.fedex.aggregator.models;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.Test;
import org.springframework.core.io.ClassPathResource;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;

import static org.junit.jupiter.api.Assertions.assertEquals;

class ShipmentsTest {
    private final ObjectMapper objectMapper = new ObjectMapper();

    @Test
    public void givenSampleValues_whenDeserialized_thenInitializeTheObject() throws IOException {
        Shipments shipments = objectMapper.readValue(new ClassPathResource("models/shipmentsSample.json").getFile(), Shipments.class);
        Shipments shipmentsExpected = new Shipments();
        shipmentsExpected.put("109347263", new ArrayList<String>(Arrays.asList("box", "box", "pallet")));
        shipmentsExpected.put("123456891", null);

        assertEquals(shipmentsExpected, shipments);
    }
}