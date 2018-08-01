package com.trasier.client.impl.pubsub;

import com.spotify.google.cloud.pubsub.client.Message;
import com.spotify.google.cloud.pubsub.client.Publisher;
import com.spotify.google.cloud.pubsub.client.Pubsub;
import com.trasier.client.model.Span;
import org.junit.Test;

import java.util.UUID;

import static org.junit.Assert.*;
import static org.mockito.Mockito.*;

public class PubSubSenderTest {

    @Test
    public void shouldSendUncompressedMessageDataNull() throws Exception {
        // given
        Publisher publisher = mock(Publisher.class);
        Pubsub pubsub = mock(Pubsub.class);
        PubSubSender sut = new PubSubSender("topic", "client", pubsub, publisher);
        Span span = Span.newSpan("op", UUID.randomUUID().toString(), UUID.randomUUID().toString(), "OP").startTimestamp(1L).build();

        // when
        Message message = sut.sendSpan(span);

        // then
        assertNotNull(message);
        assertNotNull(message.data());
        assertEquals(CompressionMimeType.NONE, message.attributes().get("mime-type"));
        verify(publisher).publish("topic", message);

        sut.close();
    }

    @Test
    public void shouldSendUncompressedMessageForSmallPayloads() throws Exception {
        // given
        Publisher publisher = mock(Publisher.class);
        Pubsub pubsub = mock(Pubsub.class);
        PubSubSender sut = new PubSubSender("topic", "client", pubsub, publisher);
        Span span = Span.newSpan("op", UUID.randomUUID().toString(), UUID.randomUUID().toString(), "OP")
                .startTimestamp(1L).incomingData("hello").build();

        // when
        Message message = sut.sendSpan(span);

        // then
        assertNotNull(message);
        assertNotNull(message.data());
        assertEquals(CompressionMimeType.NONE, message.attributes().get("mime-type"));
        verify(publisher).publish("topic", message);

        sut.close();
    }

    @Test
    public void shouldCompressBigPayloads() throws Exception {
        // given
        Publisher publisher = mock(Publisher.class);
        Pubsub pubsub = mock(Pubsub.class);
        PubSubSender sut = new PubSubSender("topic", "client", pubsub, publisher);
        Span span = Span.newSpan("op", UUID.randomUUID().toString(), UUID.randomUUID().toString(), "OP").startTimestamp(1L)
                .incomingData(generateBigPayload(PubSubSender.MAX_ALLOWED_UNCOMPRESSED_PAYLOAD_SIZE_BYTES)).build();

        // when
        Message message = sut.sendSpan(span);

        // then
        assertNotNull(message);
        assertNotNull(message.data());
        assertEquals(CompressionMimeType.SNAPPY, message.attributes().get("mime-type"));
        verify(publisher).publish("topic", message);

        sut.close();
    }

    @Test
    public void shouldNotSendPayloadsExceedingLimit() throws Exception {
        // given
        Publisher publisher = mock(Publisher.class);
        Pubsub pubsub = mock(Pubsub.class);
        PubSubSender sut = new PubSubSender("topic", "client", pubsub, publisher);
        Span span = Span.newSpan("op", UUID.randomUUID().toString(), UUID.randomUUID().toString(), "OP").startTimestamp(1L)
                .incomingData(generateBigPayload(PubSubSender.MAX_ALLOWED_PAYLOAD_SIZE_BYTES)).build();

        // when
        Message message = sut.sendSpan(span);

        // then
        assertNull(message);
        verifyZeroInteractions(publisher);

        sut.close();
    }

    private String generateBigPayload(int byteLengthLimit) {
        StringBuilder sb = new StringBuilder();
        while(sb.toString().getBytes().length < byteLengthLimit) {
            sb.append("aaaaaaaaa").append(sb.toString());
        }
        return sb.toString();
    }

}
