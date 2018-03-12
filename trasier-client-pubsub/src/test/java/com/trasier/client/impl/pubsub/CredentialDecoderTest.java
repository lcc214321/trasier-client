package com.trasier.client.impl.pubsub;

import com.google.api.client.googleapis.auth.oauth2.GoogleCredential;
import org.junit.Test;

import java.security.KeyPairGenerator;
import java.security.PrivateKey;
import java.util.Base64;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;

public class CredentialDecoderTest {

    private CredentialDecoder sut = new CredentialDecoder();

    @Test(expected = RuntimeException.class)
    public void decodeWithError() {
        sut.decode("bla");
    }

    @Test
    public void shouldDecode() throws Exception {
        String serviceAccountToken = createServiceAccountToken();

        // when
        GoogleCredential credential = sut.decode(serviceAccountToken);

        // then
        assertNotNull(credential);
        assertEquals("f3f3556fe14145", credential.getServiceAccountPrivateKeyId());
    }

    private String createServiceAccountToken() throws Exception {
        PrivateKey aPrivate = KeyPairGenerator.getInstance("RSA").generateKeyPair().getPrivate();
        String privateKey = Base64.getEncoder().encodeToString(aPrivate.getEncoded());
        return Base64.getEncoder().encodeToString(createServiceAccountJson(privateKey).getBytes());
    }

    private String createServiceAccountJson(String privateKey) {
        return "{\n" +
                "  \"type\": \"service_account\",\n" +
                "  \"project_id\": \"projectid\",\n" +
                "  \"private_key_id\": \"f3f3556fe14145\",\n" +
                "  \"private_key\": \"-----BEGIN PRIVATE KEY-----\\n"+privateKey+"\\n-----END PRIVATE KEY-----\\n\",\n" +
                "  \"client_email\": \"test@gserviceaccount.com\",\n" +
                "  \"client_id\": \"1234\",\n" +
                "  \"auth_uri\": \"https://accounts.google.com/o/oauth2/auth\",\n" +
                "  \"token_uri\": \"https://accounts.google.com/o/oauth2/token\",\n" +
                "  \"auth_provider_x509_cert_url\": \"https://www.googleapis.com/oauth2/v1/certs\",\n" +
                "  \"client_x509_cert_url\": \"https://www.googleapis.com/robot/v1/metadata/x509/test.iam.gserviceaccount.com\"\n" +
                "}";
    }

}