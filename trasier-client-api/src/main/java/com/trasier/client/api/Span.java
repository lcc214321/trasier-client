package com.trasier.client.api;

import lombok.*;

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
    private String name;

    private String parentId;
    private String status;
    private Map<String, String> tags;

    private Long startTimestamp;
    private Long beginProcessingTimestamp;
    private Endpoint incomingEndpoint;
    private ContentType incomingContentType;
    private String incomingData;
    private Map<String, String> incomingHeader;

    private Long finishProcessingTimestamp;
    private Long endTimestamp;
    private Endpoint outgoingEndpoint;
    private ContentType outgoingContentType;
    private String outgoingData;
    private Map<String, String> outgoingHeader;
    private boolean cancel;

    public static SpanBuilder newSpan(String name, String conversationId, String traceId, String spanId) {
        return hiddenBuilder().name(name).conversationId(conversationId).traceId(traceId).id(spanId).status("OK");
    }

    private static SpanBuilder hiddenBuilder() {
        return new SpanBuilder();
    }
}
