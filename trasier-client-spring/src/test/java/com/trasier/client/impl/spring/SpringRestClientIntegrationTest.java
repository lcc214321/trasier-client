
package com.trasier.client.impl.spring;

import com.trasier.client.model.ContentType;
import com.trasier.client.model.Event;
import com.trasier.client.model.System;
import org.junit.Ignore;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;

import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

@RunWith(SpringJUnit4ClassRunner.class)
@ContextConfiguration(classes = {SpringConfiguration.class, SpringClientConfiguration.class})
public class SpringRestClientIntegrationTest {
    @Autowired
    private TrasierSpringClient client;

    @Test
    @Ignore
    public void sendEventOneByOne() throws InterruptedException {
        Event.Builder requestBuilder = Event.newRequestEvent(UUID.randomUUID(), new System("Lukasz"), "GIVE_50_CHF");

        requestBuilder.correlationId(UUID.randomUUID());
        requestBuilder.consumer(new System("Frank"));
        requestBuilder.contentType(ContentType.XML);
        requestBuilder.data("<chf>50</chf>");

        Event event = requestBuilder.build();
        java.lang.System.out.println(event.getConversationId().toString());

        client.sendEvent(event);
        java.lang.System.out.println("RQ: " + event);

        // application service call to trace happens here
        Thread.sleep(500);

        Event.Builder responseBuilder = Event.newResponseEvent(requestBuilder);
        responseBuilder.contentType(ContentType.XML);
        responseBuilder.error(false);
        responseBuilder.data("<response>Sorry, I'm broke!</response>");

        client.sendEvent(responseBuilder.build());
        java.lang.System.out.println("RS: " + responseBuilder.build());
    }

    @Test
    @Ignore
    public void sendEventsBulk() throws InterruptedException {

        Event.Builder requestBuilder = Event.newRequestEvent(UUID.randomUUID(), new System("Lukasz"), "GIVE_50_CHF");

        requestBuilder.correlationId(UUID.randomUUID());
        requestBuilder.consumer(new System("Frank"));
        requestBuilder.contentType(ContentType.XML);
        requestBuilder.data("<chf>50</chf>");

        Event event = requestBuilder.build();
        java.lang.System.out.println(event.getConversationId().toString());

        // application service call to trace happens here
        Thread.sleep(500);

        Event.Builder responseBuilder = Event.newResponseEvent(requestBuilder);
        responseBuilder.contentType(ContentType.XML);
        responseBuilder.error(false);
        responseBuilder.data("<response>Sorry, I'm broke!</response>");

        List<Event> events = new ArrayList<>();
        events.add(requestBuilder.build());
        events.add(responseBuilder.build());

        java.lang.System.out.println("Sending events: " + events);

        client.sendEvents(events);
    }
}