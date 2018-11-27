package com.trasier.opentracing.spring.interceptor.servlet;

import javax.servlet.ServletOutputStream;
import javax.servlet.WriteListener;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.HttpServletResponseWrapper;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.PrintWriter;

public class CachedServletResponseWrapper extends HttpServletResponseWrapper {

    private final CachedOutputStream cachedOutputStream;
    private final PrintWriter writer;

    public static CachedServletResponseWrapper create(HttpServletResponse response) throws IOException {
        return new CachedServletResponseWrapper(response);
    }

    private CachedServletResponseWrapper(HttpServletResponse response) throws IOException {
        super(response);
        cachedOutputStream = new CachedOutputStream(response.getOutputStream());
        writer = new PrintWriter(cachedOutputStream);
    }

    @Override
    public PrintWriter getWriter() {
        return writer;
    }

    @Override
    public ServletOutputStream getOutputStream() {
        return cachedOutputStream;
    }

    @Override
    public void flushBuffer() throws IOException {
        writer.flush();
        super.flushBuffer();
    }

    public byte[] getContentAsByteArray() {
        writer.flush();
        byte[] result = cachedOutputStream.out.toByteArray();
        if (GzipUtil.isGzipStream(result)) {
            return GzipUtil.decompress(result);
        }
        return result;
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
            return outputStream.isReady();
        }

        @Override
        public void setWriteListener(WriteListener writeListener) {
            outputStream.setWriteListener(writeListener);
        }
    }
}