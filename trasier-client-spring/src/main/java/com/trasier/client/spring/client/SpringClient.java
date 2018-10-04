package com.trasier.client.spring.client;

import com.trasier.client.api.Client;
import com.trasier.client.api.Span;

import java.util.List;

public interface SpringClient extends Client {
    boolean sendSpan(Span span);
    boolean sendSpans(List<Span> spans);
}