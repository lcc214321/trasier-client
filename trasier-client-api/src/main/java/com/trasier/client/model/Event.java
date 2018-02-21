package com.trasier.client.model;

import com.trasier.client.utils.Precondition;

import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

public class Event {
    private UUID conversationId;
    private UUID correlationId;
    private System producer;
    private System consumer;
    private String operation;
    private Boolean error;
    private Long timestamp;
    private EventType type;
    private ContentType contentType;
    private Long processingTime;
    private String data;
    private Map<String, String> custom = new HashMap<>();

    private Event(Builder builder) {
        Precondition.notNull(builder.conversationId, "conversationId");
        Precondition.notNull(builder.correlationId, "correlationId");
        Precondition.notNull(builder.producer, "producer");
        Precondition.notNull(builder.operation, "operation");
        this.conversationId = builder.conversationId;
        this.correlationId = builder.correlationId;
        this.producer = builder.producer;
        this.consumer = builder.consumer;
        this.operation = builder.operation;
        this.error = builder.error;
        this.timestamp = builder.timestamp;
        this.type = builder.type;
        this.contentType = builder.contentType;
        this.processingTime = builder.processingTime;
        this.data = builder.data;
        this.custom = builder.custom;
    }

    public static Builder newEvent(UUID conversationId, System producer, String operation) {
        Builder builder = new Builder();
        builder.conversationId(conversationId);
        builder.producer(producer);
        builder.operation(operation);
        builder.timestamp(java.lang.System.currentTimeMillis());
        builder.error(false);
        builder.processingTime(0L);
        return builder;
    }

    public static Builder newRequestEvent(UUID conversationId, System producer, String operation) {
        Builder builder = newEvent(conversationId, producer, operation);
        builder.type(EventType.REQUEST);
        return builder;
    }


    public static Builder newResponseEvent(Builder requestBuilder) {
        Builder builder = newEvent(requestBuilder.conversationId, requestBuilder.producer, requestBuilder.operation);
        builder.type(EventType.RESPONSE);
        builder.consumer(requestBuilder.consumer);
        builder.correlationId(requestBuilder.correlationId);
        builder.processingTime(builder.timestamp - requestBuilder.timestamp);
        return builder;
    }

    public UUID getConversationId() {
        return conversationId;
    }

    public void setConversationId(UUID conversationId) {
        this.conversationId = conversationId;
    }

    public UUID getCorrelationId() {
        return correlationId;
    }

    public void setCorrelationId(UUID correlationId) {
        this.correlationId = correlationId;
    }

    public System getProducer() {
        return producer;
    }

    public void setProducer(System producer) {
        this.producer = producer;
    }

    public System getConsumer() {
        return consumer;
    }

    public void setConsumer(System consumer) {
        this.consumer = consumer;
    }

    public String getOperation() {
        return operation;
    }

    public void setOperation(String operation) {
        this.operation = operation;
    }

    public Boolean getError() {
        return error;
    }

    public void setError(Boolean error) {
        this.error = error;
    }

    public Long getTimestamp() {
        return timestamp;
    }

    public void setTimestamp(Long timestamp) {
        this.timestamp = timestamp;
    }

    public EventType getType() {
        return type;
    }

    public void setType(EventType type) {
        this.type = type;
    }

    public ContentType getContentType() {
        return contentType;
    }

    public void setContentType(ContentType contentType) {
        this.contentType = contentType;
    }

    public Long getProcessingTime() {
        return processingTime;
    }

    public void setProcessingTime(Long processingTime) {
        this.processingTime = processingTime;
    }

    public String getData() {
        return data;
    }

    public void setData(String data) {
        this.data = data;
    }

    public Map<String, String> getCustom() {
        return custom;
    }

    public void setCustom(Map<String, String> custom) {
        this.custom = custom;
    }

    public static final class Builder {
        private UUID conversationId;
        private UUID correlationId;
        private System producer;
        private System consumer;
        private String operation;
        private Boolean error;
        private Long timestamp;
        private EventType type;
        private ContentType contentType;
        private Long processingTime;
        private String data;
        private Map<String, String> custom;

        private Builder() {
        }

        public Event build() {
            return new Event(this);
        }

        public Builder conversationId(UUID conversationId) {
            this.conversationId = conversationId;
            return this;
        }

        public Builder correlationId(UUID correlationId) {
            this.correlationId = correlationId;
            return this;
        }

        public Builder producer(System producer) {
            this.producer = producer;
            return this;
        }

        public Builder consumer(System consumer) {
            this.consumer = consumer;
            return this;
        }

        public Builder operation(String operation) {
            this.operation = operation;
            return this;
        }

        public Builder error(Boolean error) {
            this.error = error;
            return this;
        }

        public Builder timestamp(Long timestamp) {
            this.timestamp = timestamp;
            return this;
        }

        public Builder type(EventType type) {
            this.type = type;
            return this;
        }

        public Builder contentType(ContentType contentType) {
            this.contentType = contentType;
            return this;
        }

        public Builder processingTime(Long processingTime) {
            this.processingTime = processingTime;
            return this;
        }

        public Builder data(String data) {
            this.data = data;
            return this;
        }

        public Builder custom(Map<String, String> custom) {
            this.custom = custom;
            return this;
        }
    }

    @Override
    public String toString() {
        return "Event{" +
                "conversationId=" + conversationId +
                ", correlationId=" + correlationId +
                ", producer=" + producer +
                ", consumer=" + consumer +
                ", operation='" + operation + '\'' +
                ", error=" + error +
                ", timestamp=" + timestamp +
                ", type=" + type +
                ", contentType=" + contentType +
                ", processingTime=" + processingTime +
                ", data='" + data + '\'' +
                ", custom=" + custom +
                '}';
    }
}