package com.trasier.client.model;


import java.util.ArrayList;
import java.util.List;

public class TraceInfo {

    private String id;

    private Long startTimestamp;

    private Long endTimestamp;

    private List<SpanInfo> spans = new ArrayList<>();

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public Long getStartTimestamp() {
        return startTimestamp;
    }

    public void setStartTimestamp(Long startTimestamp) {
        this.startTimestamp = startTimestamp;
    }

    public Long getEndTimestamp() {
        return endTimestamp;
    }

    public void setEndTimestamp(Long endTimestamp) {
        this.endTimestamp = endTimestamp;
    }

    public List<SpanInfo> getSpans() {
        return spans;
    }

    public void setSpans(List<SpanInfo> spans) {
        this.spans = spans;
    }
}
