package com.trasier.opentracing.interceptor.spring.servlet;

import javax.servlet.ServletOutputStream;
import javax.servlet.WriteListener;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.HttpServletResponseWrapper;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.PrintWriter;

public class CachedServletResponseWrapper extends HttpServletResponseWrapper {

    private final CachedOutputStream cachedOutputStream;

    public static CachedServletResponseWrapper create(HttpServletResponse response) throws IOException {
        return new CachedServletResponseWrapper(response);
    }

    private CachedServletResponseWrapper(HttpServletResponse response) throws IOException {
        super(response);
        cachedOutputStream = new CachedOutputStream(response.getOutputStream());
    }

    @Override
    public PrintWriter getWriter() {
        return new PrintWriter(cachedOutputStream);
    }

    @Override
    public ServletOutputStream getOutputStream() {
        return cachedOutputStream;
    }

    public byte[] getContentAsByteArray() {
        return cachedOutputStream.out.toByteArray();
    }

    private static class CachedOutputStream extends ServletOutputStream {
        private final ServletOutputStream outputStream;
        private final ByteArrayOutputStream out = new ByteArrayOutputStream();

        public CachedOutputStream(ServletOutputStream outputStream) {
            this.outputStream = outputStream;
        }

        @Override
        public void write(int b) throws IOException {
            outputStream.write(b);
            out.write(b);
        }

        @Override
        public boolean isReady() {
            return true;
        }

        @Override
        public void setWriteListener(WriteListener writeListener) {

        }
    }
}