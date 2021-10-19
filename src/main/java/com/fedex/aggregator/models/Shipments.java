package com.fedex.aggregator.models;

import javax.persistence.Entity;
import java.util.ArrayList;
import java.util.HashMap;

@Entity
public class Shipments extends HashMap<String, ArrayList<String>> {
}
