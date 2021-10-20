package com.fedex.aggregator.controllers;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fedex.aggregator.aggregators.QueueAggregator;
import com.fedex.aggregator.aggregators.SimpleAggregator;
import com.fedex.aggregator.models.AggregatedResults;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.core.io.ClassPathResource;
import org.springframework.test.web.servlet.MockMvc;

import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@SpringBootTest
@AutoConfigureMockMvc
class AggregationControllerTest {
    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @Autowired
    private AggregationController aggregationController;

    @Test
    void contextLoads() {
        assertNotNull(aggregationController);
    }

    @Test
    void givenValidParams_whenQueryAggregate_thenReturn200() throws Exception {

        AggregatedResults aggregatedResults = objectMapper.readValue(new ClassPathResource("models/aggregatedResultsSample.json").getFile(), AggregatedResults.class);
        QueueAggregator mockedQueueAggregator = mock(QueueAggregator.class);
        when(mockedQueueAggregator.aggregate(any(), any(), any())).thenReturn(aggregatedResults);
        aggregationController.setAggregator(mockedQueueAggregator);

        String[] prices = {"NL", "CN"};
        String[] track = {"109347263", "123456891"};
        String[] shipments = {"109347263", "123456891"};
        mockMvc.perform(
                get("/aggregation")
                        .param("pricing", prices)
                        .param("track", track)
                        .param("shipments", shipments)
        ).andExpect(status().isOk());
    }

    @Test
    void givenValidParams_whenSimpleAggregate_thenReturn200() throws Exception {

        AggregatedResults aggregatedResults = objectMapper.readValue(new ClassPathResource("models/aggregatedResultsSample.json").getFile(), AggregatedResults.class);
        SimpleAggregator mockedSimpleAggregator = mock(SimpleAggregator.class);
        when(mockedSimpleAggregator.aggregate(any(), any(), any())).thenReturn(aggregatedResults);
        aggregationController.setSimpleAggregator(mockedSimpleAggregator);

        String[] prices = {"NL", "CN"};
        String[] track = {"109347263", "123456891"};
        String[] shipments = {"109347263", "123456891"};
        mockMvc.perform(
                get("/aggregation/simple")
                        .param("pricing", prices)
                        .param("track", track)
                        .param("shipments", shipments)
        ).andExpect(status().isOk());
    }

}