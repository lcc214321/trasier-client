package com.trasier.client.model;

import lombok.*;

import java.util.Map;

@Builder
@Getter
@Setter
@EqualsAndHashCode
@ToString
public class Context {
    @NonNull
    private String conversationId;
    @NonNull
    private String traceId;
    @NonNull
    private String spanId;
    private Map<String, ?> baggageItems;
}
