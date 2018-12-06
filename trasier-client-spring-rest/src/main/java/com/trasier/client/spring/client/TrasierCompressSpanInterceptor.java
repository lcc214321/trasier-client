package com.trasier.client.spring.client;

import com.trasier.client.api.Span;
import org.iq80.snappy.Snappy;
import org.springframework.util.StringUtils;

import java.util.Base64;

public class TrasierCompressSpanInterceptor {

    public void intercept(Span span) {
        String incomingData = span.getIncomingData();
        if (!StringUtils.isEmpty(incomingData)) {
            byte[] incomingDataBytes = Snappy.compress(incomingData.getBytes());
            String compressedData = Base64.getEncoder().encodeToString(incomingDataBytes);
            span.setIncomingData(compressedData);
        }

        String outgoingData = span.getOutgoingData();
        if (!StringUtils.isEmpty(outgoingData)) {
            byte[] outgoingDataBytes = Snappy.compress(outgoingData.getBytes());
            String compressedData = Base64.getEncoder().encodeToString(outgoingDataBytes);
            span.setOutgoingData(compressedData);
        }
    }
}
