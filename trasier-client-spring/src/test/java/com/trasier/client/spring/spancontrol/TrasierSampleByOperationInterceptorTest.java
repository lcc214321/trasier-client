package com.trasier.client.spring.spancontrol;

import com.trasier.client.api.Span;
import org.junit.Test;

import java.util.Arrays;

import static com.trasier.client.api.Span.newSpan;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;

public class TrasierSampleByOperationInterceptorTest {

    private TrasierSampleByOperationInterceptor sut;

    @Test
    public void testWhitelist() {
        // given
        TrasierSampleByOperationConfiguration configuration = new TrasierSampleByOperationConfiguration();
        configuration.setWhitelist(Arrays.asList("ping", "pong", "checkServlet"));
        sut = new TrasierSampleByOperationInterceptor(configuration);

        Span span1 = newSpan("ping", "", "", "").build();
        Span span2 = newSpan("PING", "", "", "").build();
        Span span3 = newSpan("checkServlet", "", "", "").build();

        // when / then
        assertTrue(sut.shouldSample(span1));
        assertFalse(sut.shouldSample(span2));
        assertTrue(sut.shouldSample(span3));
    }

    @Test
    public void testBlacklist() {
        // given
        TrasierSampleByOperationConfiguration configuration = new TrasierSampleByOperationConfiguration();
        configuration.setBlacklist(Arrays.asList("ping", "pong", "checkServlet"));
        sut = new TrasierSampleByOperationInterceptor(configuration);

        Span span1 = newSpan("ping", "", "", "").build();
        Span span2 = newSpan("PING", "", "", "").build();
        Span span3 = newSpan("checkServlet", "", "", "").build();

        // when / then
        assertFalse(sut.shouldSample(span1));
        assertTrue(sut.shouldSample(span2));
        assertFalse(sut.shouldSample(span3));
    }

}