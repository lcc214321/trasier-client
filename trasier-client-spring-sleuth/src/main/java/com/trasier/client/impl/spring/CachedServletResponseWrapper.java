/*
 * Copyright (C) Schweizerische Bundesbahnen SBB, 2017.
 */

package com.trasier.client.impl.spring;

import javax.servlet.ServletOutputStream;
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

    public String getCachedData() {
        return new String(cachedOutputStream.out.toByteArray());
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
    }
}