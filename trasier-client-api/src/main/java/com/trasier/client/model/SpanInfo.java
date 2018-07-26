package com.trasier.client.model;

import java.util.ArrayList;
import java.util.List;

public class SpanInfo {

    private String id;

    private List<SpanInfo> children = new ArrayList<>();

    private String operationName;

    private Boolean error;

    private Long startTimestamp;

    private Long endTimestamp;

    private Endpoint incomingEndpoint;

    private Endpoint outgoingEndpoint;

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public List<SpanInfo> getChildren() {
        return children;
    }

    public void setChildren(List<SpanInfo> children) {
        this.children = children;
    }

    public String getOperationName() {
        return operationName;
    }

    public void setOperationName(String operationName) {
        this.operationName = operationName;
    }

    public Boolean getError() {
        return error;
    }

    public void setError(Boolean error) {
        this.error = error;
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

    public Endpoint getIncomingEndpoint() {
        return incomingEndpoint;
    }

    public void setIncomingEndpoint(Endpoint incomingEndpoint) {
        this.incomingEndpoint = incomingEndpoint;
    }

    public Endpoint getOutgoingEndpoint() {
        return outgoingEndpoint;
    }

    public void setOutgoingEndpoint(Endpoint outgoingEndpoint) {
        this.outgoingEndpoint = outgoingEndpoint;
    }
}
