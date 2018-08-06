package com.trasier.client.impl.pubsub;

import com.trasier.client.model.Span;
import org.junit.Test;

import java.io.IOException;
import java.util.UUID;

import static org.junit.Assert.assertTrue;

public class PubSubConverterTest {
    @Test
    public void convert() throws IOException {
        PubSubConverter sut = new PubSubConverter();
        Span.SpanBuilder builder = Span.newSpan("op", UUID.randomUUID().toString(), UUID.randomUUID().toString(), "noop").endTimestamp(1L);
        builder.incomingData(generateBigPayload(2000));
        Span span = builder.build();

        byte[] compressed = sut.compress(span);
        byte[] uncompressed = sut.getByteData(span);

        System.out.println(uncompressed.length);
        System.out.println(new String(uncompressed));
        System.out.println(compressed.length);
        System.out.println(new String(compressed));

        assertTrue(uncompressed.length > compressed.length);
    }

    private String generateBigPayload(int byteLengthLimit) {
        StringBuilder sb = new StringBuilder();
        while(sb.toString().getBytes().length < byteLengthLimit) {
            sb.append("aaaaaaaaa").append(sb.toString());
        }
        return sb.toString();
    }
}