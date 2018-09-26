package com.trasier.client.impl.spring.sleuth;

import com.trasier.client.Client;
import com.trasier.client.configuration.TrasierClientConfiguration;
import org.springframework.cloud.sleuth.Span;
import org.springframework.cloud.sleuth.SpanReporter;
import org.springframework.context.annotation.Primary;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Component;

import java.util.Map;

@Primary
@Component
public class TrasierSleuthReporter implements SpanReporter {
    private final Client client;
    private final TrasierClientConfiguration configuration;

    public TrasierSleuthReporter(Client client, TrasierClientConfiguration configuration) {
        this.client = client;
        this.configuration = configuration;
    }

    @Override
    @Async //there has to be sth better than this
    public void report(Span span) {
        Map<String, String> tags = span.tags();
        String conversationId = tags.get(TrasierSleuthConstants.TAG_CONVERSATION_ID);
        if(conversationId != null) {
//            String incoming = tags.get("sender");
            String operationName = tags.get(TrasierSleuthConstants.TAG_OPERATION_NAME);
            String traceId = span.traceIdString();

            com.trasier.client.model.Span.SpanBuilder builder = com.trasier.client.model.Span.newSpan(operationName, conversationId, traceId, Span.idToHex(span.getSpanId()));
            if (!span.getParents().isEmpty()) {
                builder.parentId(Span.idToHex(span.getParents().iterator().next()));
            }

            builder.startTimestamp(span.getBegin());
            builder.endTimestamp(span.getEnd());
            builder.incomingData(tags.get(TrasierSleuthConstants.TAG_REQUEST_MESSAGE));
            builder.outgoingData(tags.get(TrasierSleuthConstants.TAG_RESPONSE_MESSAGE));
            builder.status(Boolean.valueOf(tags.get(TrasierSleuthConstants.TAG_RESPONSE_IS_ERROR)) ? "ERROR" : "OK");
//            builder.outgoingEndpoint(new Endpoint(tags.get("empfaenger")));
            client.sendSpan(configuration.getAccountId(), configuration.getSpaceKey(), builder.build());
        }
    }
}
