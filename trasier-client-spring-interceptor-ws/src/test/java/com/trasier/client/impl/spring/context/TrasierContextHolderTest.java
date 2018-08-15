package com.trasier.client.impl.spring.context;

import com.trasier.client.model.Span;
import org.junit.After;
import org.junit.Test;

import java.util.UUID;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNull;
import static org.junit.Assert.assertTrue;

public class TrasierContextHolderTest {

    @After
    public void cleanup() {
        TrasierContextHolder.clear();
        assertFalse(TrasierContextHolder.isTracing());
    }

    @Test
    public void testCloseSingleSpan() throws Exception {
        // given
        String conversationId = UUID.randomUUID().toString();
        String traceId = UUID.randomUUID().toString();
        Span span = createSpan("test", conversationId, traceId);

        // when
        TrasierContextHolder.setSpan(span);
        Span currentSpan = TrasierContextHolder.getSpan();

        // then
        assertEquals(currentSpan, span);
        assertTrue(TrasierContextHolder.isTracing());

        // when
        TrasierContextHolder.closeSpan();

        // then
        assertNull(TrasierContextHolder.getSpan());
        assertFalse(TrasierContextHolder.isTracing());
    }

    @Test
    public void testCloseSpanWithParent() throws Exception {
        // given
        String conversationId = UUID.randomUUID().toString();
        String traceId = UUID.randomUUID().toString();
        Span parentSpan = createSpan("test", conversationId, traceId);
        Span childSpan = createSpan("test", conversationId, traceId);

        TrasierContextHolder.setSpan(parentSpan);
        TrasierContextHolder.setSpan(childSpan);

        // when
        Span currentSpanFromCtx = TrasierContextHolder.getSpan();

        // then
        assertEquals(childSpan, currentSpanFromCtx);
        assertTrue(TrasierContextHolder.isTracing());

        // when
        TrasierContextHolder.closeSpan();

        // then
        currentSpanFromCtx = TrasierContextHolder.getSpan();
        assertEquals(parentSpan, currentSpanFromCtx);
        assertTrue(TrasierContextHolder.isTracing());
    }

    @Test
    public void testSetSameSpanMultipleTimes() throws Exception {
        // given
        String conversationId = UUID.randomUUID().toString();
        String traceId = UUID.randomUUID().toString();
        Span span = createSpan("test", conversationId, traceId);

        // when
        TrasierContextHolder.setSpan(span);
        TrasierContextHolder.setSpan(span);
        TrasierContextHolder.setSpan(span);

        // then
        assertEquals(span, TrasierContextHolder.getSpan());

        // when
        TrasierContextHolder.closeSpan();

        // then
        assertNull(TrasierContextHolder.getSpan());
    }

    private Span createSpan(String operationName, String conversationId, String traceId) {
        return Span.newSpan("test", conversationId, traceId, UUID.randomUUID().toString()).build();
    }
}