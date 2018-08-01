package com.trasier.client.impl.spring4.client;

import com.trasier.client.configuration.TrasierClientConfiguration;
import com.trasier.client.impl.spring4.TrasierSpringConfiguration;
import com.trasier.client.model.Span;
import org.junit.Test;
import org.mockito.Mockito;

import java.util.Collections;

import static org.junit.Assert.*;

public class SpringRestCacheClientTest {
    private SpringRestClient client = Mockito.mock(SpringRestClient.class);
    private TrasierSpringConfiguration springConfiguration = new TrasierSpringConfiguration();
    private TrasierClientConfiguration clientConfig = new TrasierClientConfiguration();

    public SpringRestCacheClientTest() {
        springConfiguration.setQueueSize(1);
        springConfiguration.setQueueSizeErrorThresholdMultiplicator(2);
    }

    @Test
    public void sendSpanFullQueue() throws InterruptedException {
        SpringRestCacheClient sut = new SpringRestCacheClient(clientConfig, springConfiguration, client);

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
        SpringRestCacheClient sut = new SpringRestCacheClient(clientConfig, springConfiguration, client);

        Span span = Span.newSpan("op", "con", "tra", "spa").build();

        boolean result = sut.sendSpan(span);

        assertTrue(result);

        Thread.sleep(100L);
        Mockito.verify(client, Mockito.times(0)).sendSpans(Mockito.anyList());
        sut.close();
    }

    @Test
    public void sendSpan() throws InterruptedException {
        SpringRestCacheClient sut = new SpringRestCacheClient(clientConfig, springConfiguration, client);

        Span span = Span.newSpan("op", "con", "tra", "spa").build();

        boolean result = sut.sendSpan(span);

        assertTrue(result);

        Thread.sleep(1500L);
        Mockito.verify(client, Mockito.times(1)).sendSpans(Mockito.anyList());
        sut.close();
    }

    @Test
    public void sendSpans() throws InterruptedException {
        SpringRestCacheClient sut = new SpringRestCacheClient(clientConfig, springConfiguration, client);

        Span span = Span.newSpan("op", "con", "tra", "spa").build();

        boolean result = sut.sendSpans(Collections.singletonList(span));

        assertTrue(result);

        Thread.sleep(1500L);
        Mockito.verify(client, Mockito.times(1)).sendSpans(Mockito.anyList());
        sut.close();
    }

    @Test
    public void sendSpanAll() throws InterruptedException {
        SpringRestCacheClient sut = new SpringRestCacheClient(clientConfig, springConfiguration, client);

        Span span = Span.newSpan("op", "con", "tra", "spa").build();

        boolean result = sut.sendSpan("", "", span);

        assertTrue(result);

        Thread.sleep(1500L);
        Mockito.verify(client, Mockito.times(1)).sendSpans(Mockito.anyList());
        sut.close();
    }

    @Test
    public void sendSpansAll() throws InterruptedException {
        SpringRestCacheClient sut = new SpringRestCacheClient(clientConfig, springConfiguration, client);

        Span span = Span.newSpan("op", "con", "tra", "spa").build();

        boolean result = sut.sendSpans("", "", Collections.singletonList(span));

        assertTrue(result);

        Thread.sleep(1500L);
        Mockito.verify(client, Mockito.times(1)).sendSpans(Mockito.anyList());
        sut.close();
    }
}