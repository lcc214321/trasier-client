package com.trasier.client.impl.pubsub;

import com.spotify.google.cloud.pubsub.client.Publisher;
import com.spotify.google.cloud.pubsub.client.Pubsub;
import com.trasier.client.model.Endpoint;
import com.trasier.client.model.Span;
import org.junit.Before;
import org.junit.Test;
import org.mockito.BDDMockito;

import java.util.Arrays;
import java.util.UUID;

import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

public class PubSubClientTest {

    private PubSubClient sut;
    private Publisher publisher;
    private Pubsub pubsub;

    @Before
    public void setup() {
        publisher = mock(Publisher.class);
        pubsub = mock(Pubsub.class);
        sut = PubSubClient.builder()
                .project("trasier-project")
                .topic("trasier-topic")
                .spaceId("trasier-spaceId")
                .publisher(publisher)
                .pubsub(pubsub)
                .build();
    }

    @Test
    public void shouldNotThrowExceptionOnClose() {
        // giben
        BDDMockito.doThrow(new RuntimeException("oops")).when(publisher).close();
        // when
        sut.close();
        // then
        verify(publisher).close();
        verify(pubsub).close();
    }

    @Test
    public void shouldCloseThePublisherAndPubsub() {
        // when
        sut.close();
        // then
        verify(publisher).close();
        verify(pubsub).close();
    }

    @Test
    public void shouldThrowExceptionWhenClientNotConfigured() {
        assertTrue("project is missing", exceptionThrown(PubSubClient.builder()));
        assertTrue("topic is missing", exceptionThrown(PubSubClient.builder().project("proj")));
        assertTrue("spaceId is missing", exceptionThrown(PubSubClient.builder().project("proj").topic("topic")));

        assertFalse(exceptionThrown(PubSubClient.builder().project("proj").topic("topic").spaceId("spaceId")));
    }

    @Test
    public void shouldSendRequestAndConfirm() {
        // given
        Span span = Span.newSpan(UUID.randomUUID().toString(), UUID.randomUUID().toString(), new Endpoint("ola"), "1").endTimestamp(1L).build();

        // when
        boolean result = sut.sendSpan(span);

        // then
        assertTrue(result);
    }

    @Test
    public void shouldContinueSendingWhileExceptionOccur() {
        // given
        Span span1 = Span.newSpan(UUID.randomUUID().toString(), UUID.randomUUID().toString(), new Endpoint("ola"), "1").startTimestamp(1L).build();
        Span span2 = Span.newSpan(UUID.randomUUID().toString(), UUID.randomUUID().toString(), new Endpoint("ola"), "2").startTimestamp(1L).build();
        Span span3 = Span.newSpan(UUID.randomUUID().toString(), UUID.randomUUID().toString(), new Endpoint("ola"), "3").startTimestamp(1L).build();

        when(publisher.publish(any(), any())).thenReturn(null).thenThrow(new RuntimeException("oops")).thenReturn(null);

        // when
        boolean result = sut.sendSpans(Arrays.asList(span1, span2, span3));

        // then
        assertFalse(result);
        verify(publisher, times(3)).publish(any(), any());
    }


    private boolean exceptionThrown(PubSubClient.Builder builder) {
        try{
            builder.pubsub(pubsub).publisher(publisher).build();
        } catch(Exception e) {
            return true;
        }
        return false;
    }

}