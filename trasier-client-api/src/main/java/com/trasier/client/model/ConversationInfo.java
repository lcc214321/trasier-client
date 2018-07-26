package com.trasier.client.model;


import java.util.ArrayList;
import java.util.List;

public class ConversationInfo {

    private String id;

    private Long startTimestamp;

    private Long endTimestamp;

    private List<TraceInfo> traces = new ArrayList<>();


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

    public List<TraceInfo> getTraces() {
        return traces;
    }

    public void setTraces(List<TraceInfo> traces) {
        this.traces = traces;
    }
}
