package com.trasier.client.impl.pubsub;

import com.spotify.google.cloud.pubsub.client.Publisher;
import com.spotify.google.cloud.pubsub.client.Pubsub;
import com.trasier.client.model.Application;
import com.trasier.client.model.Event;
import org.junit.Before;
import org.junit.Test;
import org.mockito.BDDMockito;

import java.util.Arrays;
import java.util.UUID;

import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

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
                .clientId("trasier-client")
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
        assertTrue("clientId is missing", exceptionThrown(PubSubClient.builder().project("proj").topic("topic")));

        assertFalse(exceptionThrown(PubSubClient.builder().project("proj").topic("topic").clientId("cid")));
    }

    @Test
    public void shouldSendRequestAndConfirm() {
        // given
        Event event = Event.newEvent(UUID.randomUUID(), new Application("ola"), "1").correlationId(UUID.randomUUID()).build();

        // when
        boolean result = sut.sendEvent(event);

        // then
        assertTrue(result);
    }

    @Test
    public void shouldContinueSendingWhileExceptionOccur() {
        // given
        Event event1 = Event.newEvent(UUID.randomUUID(), new Application("ola"), "1").correlationId(UUID.randomUUID()).build();
        Event event2 = Event.newEvent(UUID.randomUUID(), new Application("ola"), "2").correlationId(UUID.randomUUID()).build();
        Event event3 = Event.newEvent(UUID.randomUUID(), new Application("ola"), "3").correlationId(UUID.randomUUID()).build();

        when(publisher.publish(any(), any())).thenReturn(null).thenThrow(new RuntimeException("oops")).thenReturn(null);

        // when
        boolean result = sut.sendEvents(Arrays.asList(event1, event2, event3));

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