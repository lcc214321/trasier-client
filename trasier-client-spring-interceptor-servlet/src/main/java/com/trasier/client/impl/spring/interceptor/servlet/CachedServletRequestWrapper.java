package com.trasier.client.impl.spring.interceptor.servlet;

import org.apache.commons.io.IOUtils;

import javax.servlet.ReadListener;
import javax.servlet.ServletInputStream;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletRequestWrapper;
import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;

public class CachedServletRequestWrapper extends HttpServletRequestWrapper {

    private final byte[] data;

    public static CachedServletRequestWrapper create(HttpServletRequest request) throws IOException {
        return new CachedServletRequestWrapper(request, readAll(request));
    }

    private static byte[] readAll(HttpServletRequest request) throws IOException {
        ByteArrayOutputStream byteBody = new ByteArrayOutputStream();
        IOUtils.copy(request.getInputStream(), byteBody);
        return byteBody.toByteArray();
    }

    private CachedServletRequestWrapper(HttpServletRequest request, byte[] data) {
        super(request);
        this.data = data;
    }

    @Override
    public ServletInputStream getInputStream() {
        return new ByteArrayServletInputStream(new ByteArrayInputStream(data));
    }

    public byte[] getContentAsByteArray() {
        return data;
    }

    public static class ByteArrayServletInputStream extends ServletInputStream {
        private final ByteArrayInputStream byteArrayInputStream;

        public ByteArrayServletInputStream(ByteArrayInputStream byteArrayInputStream) {
            this.byteArrayInputStream = byteArrayInputStream;
        }

        @Override
        public int read() throws IOException {
            return byteArrayInputStream.read();
        }

        @Override
        public boolean isFinished() {
            return !isReady();
        }

        @Override
        public boolean isReady() {
            return byteArrayInputStream.available() > 0;
        }

        @Override
        public void setReadListener(ReadListener readListener) {

        }
    }
}