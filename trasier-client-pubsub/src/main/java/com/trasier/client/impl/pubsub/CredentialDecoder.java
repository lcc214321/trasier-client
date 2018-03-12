package com.trasier.client.impl.pubsub;

import com.google.api.client.googleapis.auth.oauth2.GoogleCredential;
import com.google.api.client.googleapis.util.Utils;

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.util.Base64;

class CredentialDecoder {

    GoogleCredential decode(String serviceAccountToken) {
        GoogleCredential result;
        try {
            byte[] decode = Base64.getDecoder().decode(serviceAccountToken);
            ByteArrayInputStream inputStream = new ByteArrayInputStream(decode);
            result = GoogleCredential.fromStream(inputStream, Utils.getDefaultTransport(), Utils.getDefaultJsonFactory());
        } catch (IOException e) {
            throw new RuntimeException("Could not resolve credentials from token", e);
        }
        return result;
    }

}
