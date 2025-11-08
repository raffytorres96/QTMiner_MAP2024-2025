package com.example.qtereshold;

import java.util.List;

public class ParsedCluster {

    private final int id;
    private final String centroid;
    private final List<String> examples;
    private final Double avgDistance;

    public ParsedCluster(int id, String centroid, List<String> examples, Double avgDistance) {
        this.id = id;
        this.centroid = centroid;
        this.examples = examples;
        this.avgDistance = avgDistance;
    }

    // Getters
    public int getId() {
        return id;
    }

    public String getCentroid() {
        return centroid;
    }

    public List<String> getExamples() {
        return examples;
    }

    public Double getAvgDistance() {
        return avgDistance;
    }
}