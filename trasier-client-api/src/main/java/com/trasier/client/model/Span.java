package com.trasier.client.model;

import com.trasier.client.utils.Precondition;

public class Span {
    private String id;
    private String parentId;
    private String traceId;
    private String conversationId;
    private String operationName;
    private Boolean error;
    private Long startTimestamp;
    private Long beginProcessingTimestamp;
    private Endpoint incomingEndpoint;
    private ContentType incomingContentType;
    private String incomingData;
    private Long finishProcessingTimestamp;
    private Long endTimestamp;
    private Endpoint outgoingEndpoint;
    private ContentType outgoingContentType;
    private String outgoingData;

    private Span(Builder builder) {
        Precondition.notNull(builder.id, "id");
        Precondition.notNull(builder.traceId, "traceId");
        Precondition.notNull(builder.conversationId, "conversationId");
        Precondition.notNull(builder.operationName, "operationName");
        Precondition.notNull(builder.error, "error");

        this.id = builder.id;
        this.parentId = builder.parentId;
        this.traceId = builder.traceId;
        this.conversationId = builder.conversationId;
        this.operationName = builder.operationName;
        this.error = builder.error;
        this.startTimestamp = builder.startTimestamp;
        this.beginProcessingTimestamp = builder.beginProcessingTimestamp;
        this.incomingEndpoint = builder.incomingEndpoint;
        this.incomingContentType = builder.incomingContentType;
        this.incomingData = builder.incomingData;
        this.finishProcessingTimestamp = builder.finishProcessingTimestamp;
        this.endTimestamp = builder.endTimestamp;
        this.outgoingEndpoint = builder.outgoingEndpoint;
        this.outgoingContentType = builder.outgoingContentType;
        this.outgoingData = builder.outgoingData;
    }

    public static Builder newSpan(String operationName, String conversationId, String traceId, String spanId) {
        Builder builder = new Builder();
        builder.id(spanId);
        builder.conversationId(conversationId);
        builder.traceId(traceId);
        builder.operationName(operationName);
        builder.error(false);
        return builder;
    }

    public String getId() {
        return id;
    }

    public String getParentId() {
        return parentId;
    }

    public String getTraceId() {
        return traceId;
    }

    public String getConversationId() {
        return conversationId;
    }

    public String getOperationName() {
        return operationName;
    }

    public void setOperationName(String operationName) {
        Precondition.notNull(operationName, "operationName");
        this.operationName = operationName;
    }

    public Boolean getError() {
        return error;
    }

    public Long getStartTimestamp() {
        return startTimestamp;
    }

    public Long getBeginProcessingTimestamp() {
        return beginProcessingTimestamp;
    }

    public Endpoint getIncomingEndpoint() {
        return incomingEndpoint;
    }

    public void setIncomingEndpoint(Endpoint incomingEndpoint) {
        this.incomingEndpoint = incomingEndpoint;
    }

    public ContentType getIncomingContentType() {
        return incomingContentType;
    }

    public String getIncomingData() {
        return incomingData;
    }

    public Long getFinishProcessingTimestamp() {
        return finishProcessingTimestamp;
    }

    public Long getEndTimestamp() {
        return endTimestamp;
    }

    public Endpoint getOutgoingEndpoint() {
        return outgoingEndpoint;
    }

    public ContentType getOutgoingContentType() {
        return outgoingContentType;
    }

    public String getOutgoingData() {
        return outgoingData;
    }

    public void setParentId(String parentId) {
        this.parentId = parentId;
    }

    public void setError(Boolean error) {
        this.error = error;
    }

    public void setStartTimestamp(Long startTimestamp) {
        this.startTimestamp = startTimestamp;
    }

    public void setBeginProcessingTimestamp(Long beginProcessingTimestamp) {
        this.beginProcessingTimestamp = beginProcessingTimestamp;
    }

    public void setIncomingContentType(ContentType incomingContentType) {
        this.incomingContentType = incomingContentType;
    }

    public void setIncomingData(String incomingData) {
        this.incomingData = incomingData;
    }

    public void setFinishProcessingTimestamp(Long finishProcessingTimestamp) {
        this.finishProcessingTimestamp = finishProcessingTimestamp;
    }

    public void setEndTimestamp(Long endTimestamp) {
        this.endTimestamp = endTimestamp;
    }

    public void setOutgoingEndpoint(Endpoint outgoingEndpoint) {
        this.outgoingEndpoint = outgoingEndpoint;
    }

    public void setOutgoingContentType(ContentType outgoingContentType) {
        this.outgoingContentType = outgoingContentType;
    }

    public void setOutgoingData(String outgoingData) {
        this.outgoingData = outgoingData;
    }

    @Override
    public String toString() {
        return "Span{" +
                "id='" + id + '\'' +
                ", parentId='" + parentId + '\'' +
                ", traceId='" + traceId + '\'' +
                ", conversationId='" + conversationId + '\'' +
                ", operationName='" + operationName + '\'' +
                ", error=" + error +
                ", startTimestamp=" + startTimestamp +
                ", beginProcessingTimestamp=" + beginProcessingTimestamp +
                ", incomingEndpoint=" + incomingEndpoint +
                ", incomingContentType=" + incomingContentType +
                ", incomingData='" + incomingData + '\'' +
                ", finishProcessingTimestamp=" + finishProcessingTimestamp +
                ", endTimestamp=" + endTimestamp +
                ", outgoingEndpoint=" + outgoingEndpoint +
                ", outgoingContentType=" + outgoingContentType +
                ", outgoingData='" + outgoingData + '\'' +
                '}';
    }

    public static final class Builder {
        private String id;
        private String parentId;
        private String traceId;
        private String conversationId;
        private String operationName;
        private Boolean error;
        private Long startTimestamp;
        private Long beginProcessingTimestamp;
        private Endpoint incomingEndpoint;
        private ContentType incomingContentType;
        private String incomingData;
        private Long finishProcessingTimestamp;
        private Long endTimestamp;
        private Endpoint outgoingEndpoint;
        private ContentType outgoingContentType;
        private String outgoingData;

        private Builder() {
        }

        public Span build() {
            return new Span(this);
        }

        public Builder id(String id) {
            this.id = id;
            return this;
        }

        public Builder parentId(String parentId) {
            this.parentId = parentId;
            return this;
        }

        public Builder traceId(String traceId) {
            this.traceId = traceId;
            return this;
        }

        public Builder conversationId(String conversationId) {
            this.conversationId = conversationId;
            return this;
        }

        public Builder operationName(String operationName) {
            this.operationName = operationName;
            return this;
        }

        public Builder error(Boolean error) {
            this.error = error;
            return this;
        }

        public Builder startTimestamp(Long startTimestamp) {
            this.startTimestamp = startTimestamp;
            return this;
        }

        public Builder beginProcessingTimestamp(Long beginProcessingTimestamp) {
            this.beginProcessingTimestamp = beginProcessingTimestamp;
            return this;
        }

        public Builder incomingEndpoint(Endpoint incomingEndpoint) {
            this.incomingEndpoint = incomingEndpoint;
            return this;
        }

        public Builder incomingContentType(ContentType incomingContentType) {
            this.incomingContentType = incomingContentType;
            return this;
        }

        public Builder incomingData(String incomingData) {
            this.incomingData = incomingData;
            return this;
        }

        public Builder finishProcessingTimestamp(Long finishProcessingTimestamp) {
            this.finishProcessingTimestamp = finishProcessingTimestamp;
            return this;
        }

        public Builder endTimestamp(Long endTimestamp) {
            this.endTimestamp = endTimestamp;
            return this;
        }

        public Builder outgoingEndpoint(Endpoint outgoingEndpoint) {
            this.outgoingEndpoint = outgoingEndpoint;
            return this;
        }

        public Builder outgoingContentType(ContentType outgoingContentType) {
            this.outgoingContentType = outgoingContentType;
            return this;
        }

        public Builder outgoingData(String outgoingData) {
            this.outgoingData = outgoingData;
            return this;
        }
    }
}
