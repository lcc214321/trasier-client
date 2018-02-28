package com.trasier.client.impl.pubsub;

import com.trasier.client.model.ContentType;
import com.trasier.client.model.Event;
import com.trasier.client.model.System;
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
    public void sendEvents() throws InterruptedException {
        PubSubClient client = PubSubClient.builder()
                .project(java.lang.System.getProperty("trasier.pubsub.project"))
                .topic(java.lang.System.getProperty("trasier.pubsub.topic"))
                .clientId(java.lang.System.getProperty("trasier.pubsub.clientId"))
                .build();

        Event.Builder requestBuilder = Event.newRequestEvent(UUID.randomUUID(), new System("Lukasz"), "GIVE_50_CHF");

        requestBuilder.correlationId(UUID.randomUUID());
        requestBuilder.consumer(new System("Frank"));
        requestBuilder.contentType(ContentType.XML);
        requestBuilder.data("<chf>50</chf>");

        Event event = requestBuilder.build();
        java.lang.System.out.println(event.getConversationId().toString());

        client.sendEvents(Collections.singletonList(event));
        java.lang.System.out.println("RQ: " + event);

        // wait for async write
        TimeUnit.SECONDS.sleep(2);

        Event.Builder responseBuilder = Event.newResponseEvent(requestBuilder);
        responseBuilder.contentType(ContentType.XML);
        responseBuilder.error(false);
        responseBuilder.data("<response>Sorry, I'm broke!</response>");

        client.sendEvents(Collections.singletonList(responseBuilder.build()));
        java.lang.System.out.println("RS: " + responseBuilder.build());

        // wait for async write
        TimeUnit.SECONDS.sleep(2);
    }
}