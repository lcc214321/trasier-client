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
    public void testOptionsNotTraced() throws Exception {
        // given
        sut = new TrasierSampleByOperationInterceptor(new TrasierSampleByOperationConfiguration());
        Span span1 = newSpan("OPTIONS", "", "", "").build();

        // when
        sut.interceptMetdataResolved(span1);

        // then
        assertTrue(span1.isCancel());
    }

    @Test
    public void testWhitelist() {
        // given
        TrasierSampleByOperationConfiguration configuration = new TrasierSampleByOperationConfiguration();
        configuration.setWhitelist(Arrays.asList("ping", "pong", "checkServlet"));
        sut = new TrasierSampleByOperationInterceptor(configuration);

        Span span1 = newSpan("ping", "", "", "").build();
        Span span2 = newSpan("PING", "", "", "").build();
        Span span3 = newSpan("checkServlet", "", "", "").build();

        // when
        sut.interceptMetdataResolved(span1);
        sut.interceptMetdataResolved(span2);
        sut.interceptMetdataResolved(span3);

        // then
        assertFalse(span1.isCancel());
        assertTrue(span2.isCancel());
        assertFalse(span3.isCancel());
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

        // when
        sut.interceptMetdataResolved(span1);
        sut.interceptMetdataResolved(span2);
        sut.interceptMetdataResolved(span3);

        // then
        assertTrue(span1.isCancel());
        assertFalse(span2.isCancel());
        assertTrue(span3.isCancel());
    }

}