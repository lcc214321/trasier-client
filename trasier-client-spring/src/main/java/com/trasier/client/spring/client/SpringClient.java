package com.trasier.client.impl.spring.client;

import com.trasier.client.Client;
import com.trasier.client.model.Span;

import java.util.List;

public interface SpringClient extends Client {
    boolean sendSpan(Span span);
    boolean sendSpans(List<Span> spans);
}