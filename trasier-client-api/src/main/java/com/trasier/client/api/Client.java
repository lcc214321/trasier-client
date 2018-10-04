package com.trasier.client.api;

import java.util.List;

public interface Client {

    boolean sendSpan(String accountId, String spaceKey, Span span);

    boolean sendSpans(String accountId, String spaceKey, List<Span> spans);

    void close();
}