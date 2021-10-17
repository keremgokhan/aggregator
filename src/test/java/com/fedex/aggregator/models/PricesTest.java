package com.fedex.aggregator.models;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.core.io.ClassPathResource;

import java.io.IOException;

import static org.junit.jupiter.api.Assertions.assertEquals;

@SpringBootTest
class PricesTest {
    @Autowired
    private ObjectMapper objectMapper;

    @Test
    public void givenSampleValues_whenDeserialized_thenInitializeTheObject() throws IOException {
        Prices prices = objectMapper.readValue(new ClassPathResource("models/pricesSample.json").getFile(), Prices.class);
        Prices pricesExpected = new Prices();
        pricesExpected.put("NL", 14.242090605778);
        pricesExpected.put("CN", 20.503467806384);

        assertEquals(pricesExpected, prices);
    }
}