package com.fedex.aggregator.models;

import javax.persistence.Entity;
import java.util.Objects;

@Entity
public class AggregatedResults {
    private Prices pricing;
    private TrackingStatuses track;
    private Shipments shipments;

    public AggregatedResults() {
    }

    public AggregatedResults(Prices pricing, TrackingStatuses track, Shipments shipments) {
        this.pricing = pricing;
        this.track = track;
        this.shipments = shipments;
    }

    public Prices getPricing() {
        return pricing;
    }

    public void setPricing(Prices pricing) {
        this.pricing = pricing;
    }

    public TrackingStatuses getTrack() {
        return track;
    }

    public void setTrack(TrackingStatuses track) {
        this.track = track;
    }

    public Shipments getShipments() {
        return shipments;
    }

    public void setShipments(Shipments shipments) {
        this.shipments = shipments;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;

        AggregatedResults that = (AggregatedResults) o;

        if (!Objects.equals(pricing, that.pricing)) return false;
        if (!Objects.equals(track, that.track)) return false;
        return Objects.equals(shipments, that.shipments);
    }

    @Override
    public int hashCode() {
        int result = pricing != null ? pricing.hashCode() : 0;
        result = 31 * result + (track != null ? track.hashCode() : 0);
        result = 31 * result + (shipments != null ? shipments.hashCode() : 0);
        return result;
    }

    @Override
    public String toString() {
        return "AggregatedResults{" +
                "pricing=" + pricing +
                ", track=" + track +
                ", shipments=" + shipments +
                '}';
    }
}
