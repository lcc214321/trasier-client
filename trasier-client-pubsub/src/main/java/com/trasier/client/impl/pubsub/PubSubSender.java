package com.trasier.client.impl.pubsub;


import com.google.api.client.googleapis.auth.oauth2.GoogleCredential;
import com.google.common.annotations.VisibleForTesting;
import com.spotify.google.cloud.pubsub.client.Message;
import com.spotify.google.cloud.pubsub.client.MessageBuilder;
import com.spotify.google.cloud.pubsub.client.Publisher;
import com.spotify.google.cloud.pubsub.client.Pubsub;
import com.trasier.client.model.ContentType;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.Base64;

class PubSubSender {

    private static final Logger LOGGER = LoggerFactory.getLogger(PubSubClient.class);

    static final int MAX_ALLOWED_PAYLOAD_SIZE_BYTES = 1024 * 1024;
    static final int MAX_ALLOWED_UNCOMPRESSED_PAYLOAD_SIZE_BYTES = 2 * 1024;

    private String topic;
    private String clientId;
    private Pubsub pubsub;
    private Publisher publisher;
    private PubSubConverter converter;

    PubSubSender(String project, String topic, String clientId, String serviceAccountToken) {
        CredentialDecoder credentialDecoder = new CredentialDecoder();
        GoogleCredential credential = credentialDecoder.decode(serviceAccountToken);

        Pubsub pubsub = Pubsub.builder().credential(credential).build();
        Publisher publisher = Publisher.builder().pubsub(pubsub).project(project).build();
        this.initialize(topic, clientId, pubsub, publisher);
    }

    @VisibleForTesting
    PubSubSender(String topic, String clientId, Pubsub pubsub, Publisher publisher) {
        this.initialize(topic, clientId, pubsub, publisher);
    }

    private void initialize(String topic, String clientId, Pubsub pubsub, Publisher publisher) {
        this.topic = topic;
        this.clientId = clientId;
        this.publisher = publisher;
        this.pubsub = pubsub;
        this.converter = new PubSubConverter();
    }

    Message sendEvent(Event event) throws Exception {

        int payloadSize = getPayloadSize(event);

        if (payloadSize > MAX_ALLOWED_PAYLOAD_SIZE_BYTES) {
            return null;
        }

        MessageBuilder messageBuilder = Message.builder();
        messageBuilder.putAttribute("clientId", this.clientId);
        messageBuilder.putAttribute("api-version", "1");

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

    void close() {
        try {
            publisher.close();
        } catch (Exception e) {
            LOGGER.debug(e.getMessage(), e);
        }

        try {
            pubsub.close();
        } catch (Exception e) {
            LOGGER.debug(e.getMessage(), e);
        }
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
