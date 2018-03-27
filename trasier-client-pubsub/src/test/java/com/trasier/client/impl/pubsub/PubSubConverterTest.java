package com.trasier.client.impl.pubsub;

import org.junit.Test;

import java.io.IOException;
import java.util.UUID;

import static org.junit.Assert.assertTrue;

public class PubSubConverterTest {
    @Test
    public void convert() throws IOException {
        PubSubConverter sut = new PubSubConverter();
        Event.Builder builder = Event.newRequestEvent(UUID.randomUUID(), new Application("bla"), "noop");
        builder.correlationId(UUID.randomUUID());
        builder.data(generateBigPayload(2000));
        Event event = builder.build();

        byte[] compressed = sut.compressData(event);
        byte[] uncompressed = sut.getByteData(event);

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