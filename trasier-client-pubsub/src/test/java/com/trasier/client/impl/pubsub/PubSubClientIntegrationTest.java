package com.trasier.client.impl.pubsub;

import com.trasier.client.model.ContentType;
import com.trasier.client.model.Endpoint;
import com.trasier.client.model.Span;
import org.junit.Before;
import org.junit.Test;

import java.util.Collections;
import java.util.UUID;
import java.util.concurrent.TimeUnit;

import static org.junit.Assume.assumeTrue;

public class PubSubClientIntegrationTest {

    @Before
    public void setup() {
        assumeTrue("true".equals(java.lang.System.getProperty("trasier.integration-tests-on")));
    }

    @Test
    public void sendSpans() throws InterruptedException {
        PubSubClient client = PubSubClient.builder()
                .serviceAccountToken(java.lang.System.getProperty("trasier.pubsub.serviceAccountToken"))
                .project(java.lang.System.getProperty("trasier.pubsub.project"))
                .topic(java.lang.System.getProperty("trasier.pubsub.topic"))
                .spaceId(java.lang.System.getProperty("trasier.pubsub.spaceId"))
                .build();

        Span.Builder spanBuilder = Span.newSpan(UUID.randomUUID().toString(), UUID.randomUUID().toString(), new Endpoint("Lukasz"), "GIVE_50_CHF").startTimestamp(1L);

        spanBuilder.outgoingEndpoint(new Endpoint("Frank"));
        spanBuilder.incomingContentType(ContentType.XML);
        spanBuilder.incomingData("<chf>50</chf>");

        Span span = spanBuilder.build();
        java.lang.System.out.println(span.getConversationId());

        java.lang.System.out.println("RQ: " + span);

        // wait for async write
        TimeUnit.SECONDS.sleep(2);

        spanBuilder.outgoingContentType(ContentType.XML);
        spanBuilder.error(false);
        spanBuilder.outgoingData("<response>Sorry, I'm broke!</response>");

        client.sendSpans(Collections.singletonList(spanBuilder.build()));
        java.lang.System.out.println("RS: " + spanBuilder.build());

        // wait for async write
        TimeUnit.SECONDS.sleep(2);

        client.close();
    }
}