package com.trasier.client.impl.pubsub;


import com.google.api.client.googleapis.auth.oauth2.GoogleCredential;
import com.google.common.annotations.VisibleForTesting;
import com.spotify.google.cloud.pubsub.client.Message;
import com.spotify.google.cloud.pubsub.client.MessageBuilder;
import com.spotify.google.cloud.pubsub.client.Publisher;
import com.spotify.google.cloud.pubsub.client.Pubsub;
import com.trasier.client.model.Span;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.Base64;

class PubSubSender {

    private static final Logger LOGGER = LoggerFactory.getLogger(PubSubClient.class);

    static final int MAX_ALLOWED_PAYLOAD_SIZE_BYTES = 1024 * 1024;
    static final int MAX_ALLOWED_UNCOMPRESSED_PAYLOAD_SIZE_BYTES = 2 * 1024;

    private String topic;
    private String appId;
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

    private void initialize(String topic, String appId, Pubsub pubsub, Publisher publisher) {
        this.topic = topic;
        this.appId = appId;
        this.publisher = publisher;
        this.pubsub = pubsub;
        this.converter = new PubSubConverter();
    }

    Message sendSpan(Span span) throws Exception {

        int payloadSize = getIncomingPayloadSize(span) + getOutgoingPayloadSize(span);

        if (payloadSize > MAX_ALLOWED_PAYLOAD_SIZE_BYTES) {
            return null;
        }

        MessageBuilder messageBuilder = Message.builder();
        messageBuilder.putAttribute("appId", this.appId);
        messageBuilder.putAttribute("api-version", "1");

        if (payloadSize > MAX_ALLOWED_UNCOMPRESSED_PAYLOAD_SIZE_BYTES) {
            messageBuilder.putAttribute("mime-type", CompressionMimeType.SNAPPY);
            messageBuilder.data(Base64.getEncoder().encodeToString(converter.compress(span)));
        } else {
            messageBuilder.putAttribute("mime-type", CompressionMimeType.NONE);
            messageBuilder.data(Base64.getEncoder().encodeToString(converter.getByteData(span)));
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

    private int getIncomingPayloadSize(Span span) {
        if (span.getIncomingData() == null) {
            return 0;
        }
        return span.getIncomingData().getBytes().length;
    }

    private int getOutgoingPayloadSize(Span span) {
        if (span.getOutgoingData() == null) {
            return 0;
        }
        return span.getOutgoingData().getBytes().length;
    }
}
