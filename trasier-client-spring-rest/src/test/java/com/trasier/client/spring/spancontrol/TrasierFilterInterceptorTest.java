package com.trasier.client.spring.spancontrol;

import com.trasier.client.api.Span;
import com.trasier.client.configuration.TrasierFilterConfiguration;
import com.trasier.client.configuration.TrasierFilterConfiguration.Filter;
import com.trasier.client.configuration.TrasierFilterConfiguration.Strategy;
import org.junit.Test;

import java.util.Arrays;

import static com.trasier.client.api.Span.newSpan;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;

public class TrasierFilterInterceptorTest {

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
        sut.interceptUrlResolved(span0, "");
        sut.interceptUrlResolved(span1, "/getWeather");
        sut.interceptUrlResolved(span2, "/admin/health");
        sut.interceptUrlResolved(span3, "/checkServlet");
        sut.interceptUrlResolved(span4, "/health");

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
        sut.interceptUrlResolved(span1, "/admin/checkServlet");
        sut.interceptUrlResolved(span2, "/checkServlet");
        sut.interceptUrlResolved(cancelledSpan, "checkServlet");

        // then
        assertTrue(span1.isCancel());
        assertTrue(span2.isCancel());
        assertTrue(cancelledSpan.isCancel());
    }

    @Test
    public void testCustomConfig() {
        // given
        Filter skipUrlFilterConfig = new Filter();
        skipUrlFilterConfig.setStrategy(Strategy.cancel);
        skipUrlFilterConfig.setUrl("/admin.*");

        TrasierFilterConfiguration config = new TrasierFilterConfiguration();
        config.setFilters(Arrays.asList(skipUrlFilterConfig));
        TrasierSpanFilterInterceptor sut = new TrasierSpanFilterInterceptor(config);

        Span span1 = Span.newSpan("name", "id", "id", "id").build();
        Span span2 = Span.newSpan("name", "id", "id", "id").build();
        Span span3 = Span.newSpan("name", "id", "id", "id").build();

        // when
        sut.interceptUrlResolved(span1, "/admin/health");
        sut.interceptUrlResolved(span2, "/admin/checkServlet");
        sut.interceptUrlResolved(span3, "/info/checkInfoServlet");

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
        sut.interceptMetadataResolved(span1);

        // then
        assertTrue(span1.isCancel());
    }

    @Test
    public void testWhitelist() {
        // given
        Filter filter = new Filter();
        filter.setStrategy(Strategy.allow);
        filter.setOperation("ping|pong|checkServlet");
        TrasierFilterConfiguration configuration = new TrasierFilterConfiguration();
        configuration.setFilters(Arrays.asList(filter));
        TrasierSpanFilterInterceptor sut = new TrasierSpanFilterInterceptor(configuration);

        Span span1 = newSpan("ping", "", "", "").build();
        Span span2 = newSpan("PING", "", "", "").build();
        Span span3 = newSpan("checkServlet", "", "", "").build();

        // when
        sut.interceptMetadataResolved(span1);
        sut.interceptMetadataResolved(span2);
        sut.interceptMetadataResolved(span3);

        // then
        assertFalse(span1.isCancel());
        assertTrue(span2.isCancel());
        assertFalse(span3.isCancel());
    }

    @Test
    public void testBlacklist() {
        // given
        Filter filter = new Filter();
        filter.setStrategy(Strategy.cancel);
        filter.setOperation("ping|pong|checkServlet");
        TrasierFilterConfiguration configuration = new TrasierFilterConfiguration();
        configuration.setFilters(Arrays.asList(filter));
        TrasierSpanFilterInterceptor sut = new TrasierSpanFilterInterceptor(configuration);

        Span span1 = newSpan("ping", "", "", "").build();
        Span span2 = newSpan("PING", "", "", "").build();
        Span span3 = newSpan("checkServlet", "", "", "").build();

        // when
        sut.interceptMetadataResolved(span1);
        sut.interceptMetadataResolved(span2);
        sut.interceptMetadataResolved(span3);

        // then
        assertTrue(span1.isCancel());
        assertFalse(span2.isCancel());
        assertTrue(span3.isCancel());
    }

}