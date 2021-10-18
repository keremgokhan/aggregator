package com.fedex.aggregator.aggregators;

import com.fedex.aggregator.models.AggregatedResults;

public interface Aggregator {

    AggregatedResults aggregate(String[] pricing, String[] track, String[] shipments);
}
