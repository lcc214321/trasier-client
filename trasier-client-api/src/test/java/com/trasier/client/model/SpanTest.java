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

    private boolean isThrowingException(Span.Builder builder) {
        try {
            builder.build();
            return false;
        } catch (Exception e) {
            return true;
        }
    }
}