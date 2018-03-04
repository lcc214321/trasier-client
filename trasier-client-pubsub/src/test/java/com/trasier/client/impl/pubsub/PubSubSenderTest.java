package com.trasier.client.impl.pubsub;

import com.spotify.google.cloud.pubsub.client.Message;
import com.spotify.google.cloud.pubsub.client.Publisher;
import com.trasier.client.model.Application;
import com.trasier.client.model.ContentType;
import com.trasier.client.model.Event;
import org.junit.Test;

import java.util.UUID;

import static org.junit.Assert.*;
import static org.mockito.Mockito.*;

public class PubSubSenderTest {

    @Test
    public void shouldSendUncompressedMessageDataNull() throws Exception {
        // given
        Publisher publisher = mock(Publisher.class);
        PubSubSender sender = new PubSubSender("topic", "client", publisher);
        Event event = Event.newEvent(UUID.randomUUID(), new Application("A"), "OP").correlationId(UUID.randomUUID()).build();

        // when
        Message message = sender.sendEvent(event);

        // then
        assertNotNull(message);
        assertNotNull(message.data());
        assertEquals(CompressionMimeType.NONE, message.attributes().get("mime-type"));
        verify(publisher).publish("topic", message);
    }

    @Test
    public void shouldSendUncompressedMessageForSmallPayloads() throws Exception {
        // given
        Publisher publisher = mock(Publisher.class);
        PubSubSender sender = new PubSubSender("topic", "client", publisher);
        Event event = Event.newEvent(UUID.randomUUID(), new Application("A"), "OP")
                .data("hello")
                .correlationId(UUID.randomUUID()).build();

        // when
        Message message = sender.sendEvent(event);

        // then
        assertNotNull(message);
        assertNotNull(message.data());
        assertEquals(CompressionMimeType.NONE, message.attributes().get("mime-type"));
        verify(publisher).publish("topic", message);
    }

    @Test
    public void shouldCompressBigPayloads() throws Exception {
        // given
        Publisher publisher = mock(Publisher.class);
        PubSubSender sender = new PubSubSender("topic", "client", publisher);
        Event event = Event.newEvent(UUID.randomUUID(), new Application("A"), "OP")
                .data(generateBigPayload(PubSubSender.MAX_ALLOWED_UNCOMPRESSED_PAYLOAD_SIZE_BYTES))
                .correlationId(UUID.randomUUID()).build();

        // when
        Message message = sender.sendEvent(event);

        // then
        assertNotNull(message);
        assertNotNull(message.data());
        assertEquals(CompressionMimeType.SNAPPY, message.attributes().get("mime-type"));
        verify(publisher).publish("topic", message);
    }

    @Test
    public void shouldNotCompressBigPayloadsWithUnsupportedType() throws Exception {
        // given
        Publisher publisher = mock(Publisher.class);
        PubSubSender sender = new PubSubSender("topic", "client", publisher);
        Event event = Event.newEvent(UUID.randomUUID(), new Application("A"), "OP")
                .contentType(ContentType.ENCRYPTED)
                .data(generateBigPayload(PubSubSender.MAX_ALLOWED_UNCOMPRESSED_PAYLOAD_SIZE_BYTES))
                .correlationId(UUID.randomUUID()).build();

        // when
        Message message = sender.sendEvent(event);

        // then
        assertNotNull(message);
        assertNotNull(message.data());
        assertEquals(CompressionMimeType.NONE, message.attributes().get("mime-type"));
        verify(publisher).publish("topic", message);
    }

    @Test
    public void shouldNotSendPayloadsExceedingLimit() throws Exception {
        // given
        Publisher publisher = mock(Publisher.class);
        PubSubSender sender = new PubSubSender("topic", "client", publisher);
        Event event = Event.newEvent(UUID.randomUUID(), new Application("A"), "OP")
                .data(generateBigPayload(PubSubSender.MAX_ALLOWED_PAYLOAD_SIZE_BYTES))
                .correlationId(UUID.randomUUID()).build();

        // when
        Message message = sender.sendEvent(event);

        // then
        assertNull(message);
        verifyZeroInteractions(publisher);
    }

    private String generateBigPayload(int byteLengthLimit) {
        StringBuilder sb = new StringBuilder();
        while(sb.toString().getBytes().length < byteLengthLimit) {
            sb.append("aaaaaaaaa").append(sb.toString());
        }
        return sb.toString();
    }

}