
package com.trasier.client.impl.spring4;

import com.trasier.client.model.ContentType;
import com.trasier.client.model.Endpoint;
import com.trasier.client.model.Span;
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
@ContextConfiguration(classes = {TrasierSpringConfiguration.class, SpringClientConfiguration.class})
public class SpringRestClientIntegrationTest {
    @Autowired
    private SpringRestClient client;

    @Test
    @Ignore
    public void sendSpanOneByOne() throws InterruptedException {
        Span.Builder spanBuilder = Span.newSpan(UUID.randomUUID().toString(), UUID.randomUUID().toString(), new Endpoint("Lukasz"), "GIVE_50_CHF").startTimestamp(1L);

        spanBuilder.incomingEndpoint(new Endpoint("Frank"));
        spanBuilder.incomingContentType(ContentType.XML);
        spanBuilder.incomingData("<chf>50</chf>");

        Span span = spanBuilder.build();
        java.lang.System.out.println(span.getConversationId());

        java.lang.System.out.println("RQ: " + span);

        // application service call to trace happens here
        Thread.sleep(500);

        spanBuilder.outgoingContentType(ContentType.XML);
        spanBuilder.error(false);
        spanBuilder.outgoingData("<response>Sorry, I'm broke!</response>");

        client.sendSpan(spanBuilder.build());
        java.lang.System.out.println("RS: " + spanBuilder.build());
    }

    @Test
    @Ignore
    public void sendSpansBulk() throws InterruptedException {

        Span.Builder spanBuilder = Span.newSpan(UUID.randomUUID().toString(), UUID.randomUUID().toString(), new Endpoint("Lukasz"), "GIVE_50_CHF").endTimestamp(1L);

        spanBuilder.incomingEndpoint(new Endpoint("Frank"));
        spanBuilder.incomingContentType(ContentType.XML);
        spanBuilder.incomingData("<chf>50</chf>");

        Span span = spanBuilder.build();
        java.lang.System.out.println(span.getConversationId());

        // application service call to trace happens here
        Thread.sleep(500);

        spanBuilder.outgoingContentType(ContentType.XML);
        spanBuilder.error(false);
        spanBuilder.outgoingData("<response>Sorry, I'm broke!</response>");

        List<Span> spans = new ArrayList<>();
        spans.add(spanBuilder.build());
        spanBuilder.id(UUID.randomUUID().toString());
        spans.add(spanBuilder.build());

        java.lang.System.out.println("Sending spans: " + spans);

        client.sendSpans(spans);
    }
}