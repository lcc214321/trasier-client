package com.trasier.client.model;

import org.junit.Test;

import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;

public class SpanTest {

    @Test
    public void shoudlCheckMandatoryFields() {
        assertTrue(isThrowingException(Span.newSpan(null, null, null, null)));
        assertTrue(isThrowingException(Span.newSpan("1", null, null, null)));
        assertTrue(isThrowingException(Span.newSpan("1", "2", new Endpoint("Test"), null)));

        assertFalse(isThrowingException(Span.newSpan("1", "2", new Endpoint("Test"), "TestOp")));
        assertFalse(isThrowingException(Span.newSpan("1", "2", new Endpoint("Test"), "TestOp")
                .outgoingEndpoint(new Endpoint("Consumer"))
        ));
    }

//    @Test
//    public void shouldPopulateFieldsFromProducer() {
//        // given
//        UUID conversationId = UUID.randomUUID();
//        UUID correlationId = UUID.randomUUID();
//        Span.Builder requestBuilder = Span.newRequestSpan(conversationId, new Application("TestProducer"), "TestOp")
//                .correlationId(correlationId)
//                .consumer(new Application("TestConsumer"));
//        Span request = requestBuilder.build();
//
//        // when
//        Span response = Span.newResponseSpan(requestBuilder).build();
//
//        // then
//        assertEquals(SpanType.REQUEST, request.getType());
//        assertEquals(SpanType.RESPONSE, response.getType());
//        assertFalse(request.getError());
//        assertFalse(response.getError());
//        assertTrue(response.getProcessingTime() > 0);
//        assertEquals(0, request.getProcessingTime().intValue());
//        assertEquals(conversationId, response.getConversationId());
//        assertEquals(correlationId, response.getCorrelationId());
//        assertEquals("TestProducer", response.getProducer().getName());
//        assertEquals("TestConsumer", response.getConsumer().getName());
//        assertNotNull(request.getTimestamp());
//        assertNotNull(response.getTimestamp());
//    }

    private boolean isThrowingException(Span.Builder builder) {
        try {
            builder.build();
            return false;
        } catch (Exception e) {
            return true;
        }
    }

}