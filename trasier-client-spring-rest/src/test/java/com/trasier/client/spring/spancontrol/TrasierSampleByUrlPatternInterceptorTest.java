package com.trasier.client.spring.spancontrol;

import com.trasier.client.api.Span;
import org.junit.Test;

import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;

public class TrasierSampleByUrlPatternInterceptorTest {

    @Test
    public void testDefaultConfig() {
        // given
        TrasierSampleByUrlPatternInterceptor sut = new TrasierSampleByUrlPatternInterceptor();
        Span span0 = Span.newSpan("name", "id", "id", "id").build();
        Span span1 = Span.newSpan("name", "id", "id", "id").build();
        Span span2 = Span.newSpan("name", "id", "id", "id").build();
        Span span3 = Span.newSpan("name", "id", "id", "id").build();
        Span span4 = Span.newSpan("name", "id", "id", "id").build();

        // when
        sut.interceptRequestUrlResolved(span0, "");
        sut.interceptRequestUrlResolved(span1, "/getWeather");
        sut.interceptRequestUrlResolved(span2, "/admin/health");
        sut.interceptRequestUrlResolved(span3, "/checkServlet");
        sut.interceptRequestUrlResolved(span4, "/health");

        // then
        assertFalse(span0.isCancel());
        assertFalse(span1.isCancel());
        assertFalse(span2.isCancel());
        assertTrue(span3.isCancel());
        assertTrue(span4.isCancel());
    }

    @Test
    public void testCancelledSpan() {
        // given
        TrasierSampleByUrlPatternInterceptor sut = new TrasierSampleByUrlPatternInterceptor();
        Span span1 = Span.newSpan("name", "id", "id", "id").build();
        Span span2 = Span.newSpan("name", "id", "id", "id").build();
        Span cancelledSpan = Span.newSpan("name", "id", "id", "id").build();
        cancelledSpan.setCancel(true);

        // when
        sut.interceptRequestUrlResolved(span1, "/admin/checkServlet");
        sut.interceptRequestUrlResolved(span2, "/checkServlet");
        sut.interceptRequestUrlResolved(cancelledSpan, "checkServlet");

        // then
        assertTrue(span1.isCancel());
        assertTrue(span2.isCancel());
        assertTrue(cancelledSpan.isCancel());
    }

    @Test
    public void testCustomConfig() {
        // given
        TrasierSampleByUrlPatternConfiguration config = new TrasierSampleByUrlPatternConfiguration();
        config.setSkipPattern("/admin.*");
        TrasierSampleByUrlPatternInterceptor sut = new TrasierSampleByUrlPatternInterceptor(config);

        Span span1 = Span.newSpan("name", "id", "id", "id").build();
        Span span2 = Span.newSpan("name", "id", "id", "id").build();
        Span span3 = Span.newSpan("name", "id", "id", "id").build();

        // when
        sut.interceptRequestUrlResolved(span1, "/admin/health");
        sut.interceptRequestUrlResolved(span2, "/admin/checkServlet");
        sut.interceptRequestUrlResolved(span3, "/info/checkInfoServlet");

        // then
        assertTrue(span1.isCancel());
        assertTrue(span2.isCancel());
        assertFalse(span3.isCancel());
    }

}