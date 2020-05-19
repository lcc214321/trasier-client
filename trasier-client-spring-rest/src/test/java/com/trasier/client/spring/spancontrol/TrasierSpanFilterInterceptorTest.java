package com.trasier.client.spring.spancontrol;

import com.trasier.client.api.Span;
import org.junit.Test;

import java.util.Arrays;

import static com.trasier.client.api.Span.newSpan;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;

public class TrasierSpanFilterInterceptorTest {

    @Test
    public void testDefaultConfig() {
        // given
        TrasierSpanFilterInterceptor sut = new TrasierSpanFilterInterceptor();
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
        TrasierSpanFilterInterceptor sut = new TrasierSpanFilterInterceptor();
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
        SpanFilter skipUrlFilterConfig = new SpanFilter();
        skipUrlFilterConfig.setStrategy(Strategy.cancel);
        skipUrlFilterConfig.setUrl("/admin.*");

        TrasierSpanFilterConfiguration config = new TrasierSpanFilterConfiguration();
        config.setFilters(Arrays.asList(skipUrlFilterConfig));
        TrasierSpanFilterInterceptor sut = new TrasierSpanFilterInterceptor(config);

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

    @Test
    public void testOptionsNotTraced() throws Exception {
        // given
        TrasierSpanFilterInterceptor sut = new TrasierSpanFilterInterceptor();
        Span span1 = newSpan("OPTIONS", "", "", "").build();

        // when
        sut.interceptMetdataResolved(span1);

        // then
        assertTrue(span1.isCancel());
    }

    @Test
    public void testWhitelist() {
        // given
        SpanFilter spanFilter = new SpanFilter();
        spanFilter.setStrategy(Strategy.allow);
        spanFilter.setOperation("ping|pong|checkServlet");
        TrasierSpanFilterConfiguration configuration = new TrasierSpanFilterConfiguration();
        configuration.setFilters(Arrays.asList(spanFilter));
        TrasierSpanFilterInterceptor sut = new TrasierSpanFilterInterceptor(configuration);

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
        SpanFilter spanFilter = new SpanFilter();
        spanFilter.setStrategy(Strategy.cancel);
        spanFilter.setOperation("ping|pong|checkServlet");
        TrasierSpanFilterConfiguration configuration = new TrasierSpanFilterConfiguration();
        configuration.setFilters(Arrays.asList(spanFilter));
        TrasierSpanFilterInterceptor sut = new TrasierSpanFilterInterceptor(configuration);

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