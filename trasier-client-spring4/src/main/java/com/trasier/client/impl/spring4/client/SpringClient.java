package com.trasier.client.impl.spring4.client;

import com.trasier.client.Client;
import com.trasier.client.model.ConversationInfo;
import com.trasier.client.model.Span;

import java.util.List;

public interface SpringClient extends Client {
    boolean sendSpan(Span span);
    boolean sendSpans(List<Span> spans);

    ConversationInfo readConversation(String conversationId);
    Span readSpan(String conversationId, String traceId, String spanId);
}