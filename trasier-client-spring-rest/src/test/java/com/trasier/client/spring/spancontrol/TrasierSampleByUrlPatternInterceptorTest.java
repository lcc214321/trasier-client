package com.trasier.client.spring.spancontrol;

import com.trasier.client.api.Span;
import org.junit.Test;

import java.util.HashMap;
import java.util.Map;

import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;

public class TrasierSampleByUrlPatternInterceptorTest {

    @Test
    public void testDefaultConfig() {
        // given
        TrasierSampleByUrlPatternInterceptor sut = new TrasierSampleByUrlPatternInterceptor();
        Span span = Span.newSpan("name", "id", "id", "id").build();

        // when // then
        assertTrue(sut.shouldSample(span, new HashMap<>()));
        assertTrue(sut.shouldSample(span, createUrlMap("/getWeather")));
        assertTrue(sut.shouldSample(span, createUrlMap("/admin/health")));
        assertFalse(sut.shouldSample(span, createUrlMap("/checkServlet")));
        assertFalse(sut.shouldSample(span, createUrlMap("/health")));
    }

    @Test
    public void testCancelledSpan() {
        // given
        TrasierSampleByUrlPatternInterceptor sut = new TrasierSampleByUrlPatternInterceptor();
        Span span = Span.newSpan("name", "id", "id", "id").build();
        Span cancelledSpan = Span.newSpan("name", "id", "id", "id").build();
        cancelledSpan.setCancel(true);

        // when // then
        assertFalse(sut.shouldSample(span, createUrlMap("/admin/checkServlet")));
        assertFalse(sut.shouldSample(span, createUrlMap("/checkServlet")));
        assertFalse(sut.shouldSample(cancelledSpan, createUrlMap("checkServlet")));
    }

    @Test
    public void testCustomConfig() {
        // given
        TrasierSampleByUrlPatternConfiguration config = new TrasierSampleByUrlPatternConfiguration();
        config.setSkipPattern("/admin.*");
        TrasierSampleByUrlPatternInterceptor sut = new TrasierSampleByUrlPatternInterceptor(config);

        Span span = Span.newSpan("name", "id", "id", "id").build();

        // when // then
        assertFalse(sut.shouldSample(span, createUrlMap("/admin/health")));
        assertFalse(sut.shouldSample(span, createUrlMap("/admin/checkServlet")));
        assertTrue(sut.shouldSample(span, createUrlMap("/info/checkInfoServlet")));
    }

    private Map<String, Object> createUrlMap(String url) {
        Map<String, Object> map = new HashMap<>();
        map.put("url", url);
        return map;
    }



}