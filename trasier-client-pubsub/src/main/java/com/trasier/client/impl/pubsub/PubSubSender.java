package com.trasier.client.impl.pubsub;


import com.spotify.google.cloud.pubsub.client.Message;
import com.spotify.google.cloud.pubsub.client.MessageBuilder;
import com.spotify.google.cloud.pubsub.client.Publisher;
import com.trasier.client.model.ContentType;
import com.trasier.client.model.Event;

import java.util.Base64;

class PubSubSender {

    static final int MAX_ALLOWED_PAYLOAD_SIZE_BYTES = 1024 * 1024;
    static final int MAX_ALLOWED_UNCOMPRESSED_PAYLOAD_SIZE_BYTES = 2 * 1024;

    private final String topic;
    private final String clientId;
    private final Publisher publisher;
    private final PubSubConverter converter;

    PubSubSender(String topic, String clientId, Publisher publisher) {
        this.topic = topic;
        this.clientId = clientId;
        this.publisher = publisher;
        this.converter = new PubSubConverter();
    }

    Message sendEvent(Event event) throws Exception {

        int payloadSize = getPayloadSize(event);

        if (payloadSize > MAX_ALLOWED_PAYLOAD_SIZE_BYTES) {
            return null;
        }

        MessageBuilder messageBuilder = Message.builder();
        messageBuilder.putAttribute("clientId", this.clientId);

        if (payloadSize > MAX_ALLOWED_UNCOMPRESSED_PAYLOAD_SIZE_BYTES && isCompressionSupported(event)) {
            messageBuilder.putAttribute("mime-type", CompressionMimeType.SNAPPY);
            messageBuilder.data(Base64.getEncoder().encodeToString(converter.compressData(event)));
        } else {
            messageBuilder.putAttribute("mime-type", CompressionMimeType.NONE);
            messageBuilder.data(Base64.getEncoder().encodeToString(converter.getByteData(event)));
        }

        Message message = messageBuilder.build();

        publisher.publish(topic, message);

        return message;
    }

    void shutdown() {
        publisher.close();
    }

    private boolean isCompressionSupported(Event event) {
        ContentType contentType = event.getContentType();
        if (contentType == null) {
            return true;
        }
        switch (contentType) {
            case ENCRYPTED:
                return false;
            default:
                return true;
        }
    }

    private int getPayloadSize(Event event) {
        if (event.getData() == null) {
            return 0;
        }
        return event.getData().getBytes().length;
    }
}
