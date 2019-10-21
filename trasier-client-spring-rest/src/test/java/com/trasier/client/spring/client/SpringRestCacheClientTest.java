package com.trasier.client.spring.client;

import com.trasier.client.api.Span;
import com.trasier.client.configuration.TrasierClientConfiguration;
import com.trasier.client.spring.TrasierSpringClientQueueConfiguration;
import com.trasier.client.spring.rest.TrasierSpringRestCacheClient;
import com.trasier.client.spring.rest.TrasierSpringRestClient;
import org.junit.Test;
import org.mockito.Mockito;

import java.util.Collections;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;

public class SpringRestCacheClientTest {
    private TrasierSpringRestClient client = Mockito.mock(TrasierSpringRestClient.class);
    private TrasierSpringClientQueueConfiguration springConfiguration = new TrasierSpringClientQueueConfiguration();
    private TrasierClientConfiguration clientConfig = new TrasierClientConfiguration();

    public SpringRestCacheClientTest() {
        springConfiguration.setQueueSize(1);
        springConfiguration.setQueueSizeErrorThresholdMultiplicator(2);
    }

    @Test
    public void sendSpanFullQueue() throws InterruptedException {
        TrasierSpringRestCacheClient sut = new TrasierSpringRestCacheClient(clientConfig, springConfiguration, client);

        Span span1 = Span.newSpan("op", "con", "tra", "spa").build();
        boolean result1 = sut.sendSpan(span1);
        assertTrue(result1);

        Span span2 = Span.newSpan("op", "con", "tra", "spa").build();
        boolean result2 = sut.sendSpan(span2);
        assertFalse(result2);
        assertEquals(1, sut.getCountFullQueueErrors().get());

        Span span3 = Span.newSpan("op", "con", "tra", "spa").build();
        boolean result3 = sut.sendSpan(span3);
        assertFalse(result3);
        assertEquals(0, sut.getCountFullQueueErrors().get());

        Thread.sleep(1500L);
        Mockito.verify(client, Mockito.times(1)).sendSpans(Mockito.anyList());
        sut.close();
    }

    @Test
    public void sendSpanNotYet() throws InterruptedException {
        TrasierSpringRestCacheClient sut = new TrasierSpringRestCacheClient(clientConfig, springConfiguration, client);

        Span span = Span.newSpan("op", "con", "tra", "spa").build();

        boolean result = sut.sendSpan(span);

        assertTrue(result);

        Thread.sleep(100L);
        Mockito.verify(client, Mockito.times(0)).sendSpans(Mockito.anyList());
        sut.close();
    }

    @Test
    public void sendSpan() throws InterruptedException {
        TrasierSpringRestCacheClient sut = new TrasierSpringRestCacheClient(clientConfig, springConfiguration, client);

        Span span = Span.newSpan("op", "con", "tra", "spa").build();

        boolean result = sut.sendSpan(span);

        assertTrue(result);

        Thread.sleep(1500L);
        Mockito.verify(client, Mockito.times(1)).sendSpans(Mockito.anyList());
        sut.close();
    }

    @Test
    public void sendSpans() throws InterruptedException {
        TrasierSpringRestCacheClient sut = new TrasierSpringRestCacheClient(clientConfig, springConfiguration, client);

        Span span = Span.newSpan("op", "con", "tra", "spa").build();

        boolean result = sut.sendSpans(Collections.singletonList(span));

        assertTrue(result);

        Thread.sleep(1500L);
        Mockito.verify(client, Mockito.times(1)).sendSpans(Mockito.anyList());
        sut.close();
    }

    @Test
    public void sendSpanAll() throws InterruptedException {
        TrasierSpringRestCacheClient sut = new TrasierSpringRestCacheClient(clientConfig, springConfiguration, client);

        Span span = Span.newSpan("op", "con", "tra", "spa").build();

        boolean result = sut.sendSpan("", "", span);

        assertTrue(result);

        Thread.sleep(1500L);
        Mockito.verify(client, Mockito.times(1)).sendSpans(Mockito.anyList());
        sut.close();
    }

    @Test
    public void sendSpansAll() throws InterruptedException {
        TrasierSpringRestCacheClient sut = new TrasierSpringRestCacheClient(clientConfig, springConfiguration, client);

        Span span = Span.newSpan("op", "con", "tra", "spa").build();

        boolean result = sut.sendSpans("", "", Collections.singletonList(span));

        assertTrue(result);

        Thread.sleep(1500L);
        Mockito.verify(client, Mockito.times(1)).sendSpans(Mockito.anyList());
        sut.close();
    }
}