package com.trasier.client.model;

import lombok.Builder;
import lombok.EqualsAndHashCode;
import lombok.Getter;
import lombok.NonNull;
import lombok.Setter;
import lombok.ToString;

import java.util.Map;

@Builder(builderMethodName = "hiddenBuilder")
@Getter
@Setter
@EqualsAndHashCode
@ToString
public class Span {
    @NonNull
    private String id;
    @NonNull
    private String traceId;
    @NonNull
    private String conversationId;
    @NonNull
    private String operationName;

    private String parentId;
    private Boolean error;

    private Long startTimestamp;
    private Long beginProcessingTimestamp;
    private Endpoint incomingEndpoint;
    private ContentType incomingContentType;
    private String incomingData;
    private Map<String, ?> incomingTags;

    private Long finishProcessingTimestamp;
    private Long endTimestamp;
    private Endpoint outgoingEndpoint;
    private ContentType outgoingContentType;
    private String outgoingData;
    private Map<String, ?> outgoingTags;

    public static SpanBuilder newSpan(String operationName, String conversationId, String traceId, String spanId) {
        return hiddenBuilder().operationName(operationName).conversationId(conversationId).traceId(traceId).id(spanId).error(false);
    }

    private static SpanBuilder hiddenBuilder() {
        return new SpanBuilder();
    }
}
