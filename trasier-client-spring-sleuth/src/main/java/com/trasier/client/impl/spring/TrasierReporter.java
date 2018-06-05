package com.trasier.client.impl.spring;

import com.trasier.client.Client;
import com.trasier.client.model.Endpoint;
import org.springframework.cloud.sleuth.Span;
import org.springframework.cloud.sleuth.SpanReporter;
import org.springframework.context.annotation.Primary;
import org.springframework.stereotype.Component;

import java.util.Map;

@Primary
@Component
public class TrasierReporter implements SpanReporter {
    private final Client client;

    public TrasierReporter(Client client) {
        this.client = client;
    }

    @Override
    public void report(Span span) {
        Map<String, String> tags = span.tags();
        String conversationId = tags.get(TrasierConstants.TAG_CONVERSATION_ID);
        if(conversationId != null) {
//            String incoming = tags.get("sender");
            String operationName = tags.get(TrasierConstants.TAG_OPERATION_NAME);
            String traceId = span.traceIdString();

            com.trasier.client.model.Span.Builder builder = com.trasier.client.model.Span.newSpan(conversationId, traceId, new Endpoint("in"), operationName);
            builder.id(span.idToHex(span.getSpanId()));
            if (!span.getParents().isEmpty()) {
                builder.parentId(span.idToHex(span.getParents().iterator().next()));
            }
            builder.startTimestamp(span.getBegin());
            builder.endTimestamp(span.getEnd());
            builder.incomingData(tags.get(TrasierConstants.TAG_REQUEST_MESSAGE));
            builder.outgoingData(tags.get(TrasierConstants.TAG_RESPONSE_MESSAGE));
//            builder.outgoingEndpoint(new Endpoint(tags.get("empfaenger")));
            client.sendSpan(builder.build());
        }
    }
}
