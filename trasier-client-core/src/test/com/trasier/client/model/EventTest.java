package com.trasier.client.model;

import org.junit.Test;

import java.util.UUID;

import static org.junit.Assert.*;

public class EventTest {

    @Test
    public void shoudlCheckMandatoryFields() {
        assertTrue(isThrowingException(Event.newEvent(null, null, null)));
        assertTrue(isThrowingException(Event.newEvent(UUID.randomUUID(), null, null)));
        assertTrue(isThrowingException(Event.newEvent(UUID.randomUUID(), new System("Test"), null)));
        assertTrue(isThrowingException(Event.newEvent(UUID.randomUUID(), new System("Test"), "TestOp")));

        assertTrue(isThrowingException(Event.newEvent(UUID.randomUUID(), new System("Test"), "TestOp").correlationId(UUID.randomUUID())));

        assertFalse(isThrowingException(Event.newEvent(UUID.randomUUID(), new System("Test"), "TestOp")
                .correlationId(UUID.randomUUID())
                .consumer(new System("Consumer"))
        ));
    }

    @Test
    public void shouldPopulateFieldsFromProducer() {
        // given
        UUID conversationId = UUID.randomUUID();
        UUID correlationId = UUID.randomUUID();
        Event.Builder requestBuilder = Event.newRequestEvent(conversationId, new System("TestProducer"), "TestOp")
                .correlationId(correlationId)
                .consumer(new System("TestConsumer"));
        Event request = requestBuilder.build();

        // when
        Event response = Event.newResponseEvent(requestBuilder).build();

        // then
        assertEquals(EventType.REQUEST, request.getType());
        assertEquals(EventType.RESPONSE, response.getType());
        assertFalse(request.getError());
        assertFalse(response.getError());
        assertTrue(response.getProcessingTime() > 0);
        assertEquals(0, request.getProcessingTime().intValue());
        assertEquals(conversationId, response.getConversationId());
        assertEquals(correlationId, response.getCorrelationId());
        assertEquals("TestProducer", response.getProducer().getName());
        assertEquals("TestConsumer", response.getConsumer().getName());
        assertNotNull(request.getTimestamp());
        assertNotNull(response.getTimestamp());
    }

    private boolean isThrowingException(Event.Builder builder) {
        try {
            builder.build();
            return false;
        } catch (Exception e) {
            return true;
        }
    }

}