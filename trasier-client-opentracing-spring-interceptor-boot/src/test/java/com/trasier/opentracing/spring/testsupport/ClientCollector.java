package com.trasier.opentracing.spring.testsupport;

import com.trasier.client.api.Client;
import com.trasier.client.api.Span;

import java.util.ArrayList;
import java.util.List;

public class ClientCollector implements Client {

    private List<Span> sendSpans = new ArrayList<>();

    @Override
    public boolean sendSpan(String accountId, String spaceKey, Span span) {
        sendSpans.add(span);
        return true;
    }

    @Override
    public boolean sendSpans(String accountId, String spaceKey, List<Span> spans) {
        sendSpans.addAll(spans);
        return true;
    }

    @Override
    public void close() {

    }

    public List<Span> getSendSpans() {
        return sendSpans;
    }
}
