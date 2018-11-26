package com.trasier.opentracing.spring.interceptor.servlet;

import org.apache.commons.io.IOUtils;

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.util.zip.GZIPInputStream;

public class GzipUtil {

    public static byte[] decompress(byte[] compressed) {
        try{
            return safeDecompress(compressed);
        } catch(IOException e) {
            // ignore
        }
        return new byte[0];
    }

    private static byte[] safeDecompress(byte[] compressed) throws IOException {
        ByteArrayInputStream byteArrayInputStream = new ByteArrayInputStream(compressed);
        GZIPInputStream gzipInputStream = new GZIPInputStream(byteArrayInputStream);
        return IOUtils.toByteArray(gzipInputStream);
    }
}
