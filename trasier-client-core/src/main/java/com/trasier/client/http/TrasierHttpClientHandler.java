package com.trasier.client.http;

import org.asynchttpclient.AsyncCompletionHandlerBase;
import org.asynchttpclient.HttpResponseStatus;
import org.asynchttpclient.Response;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.concurrent.atomic.AtomicLong;

public class TrasierHttpClientHandler extends AsyncCompletionHandlerBase {
    private static final Logger LOGGER = LoggerFactory.getLogger(TrasierHttpClientHandler.class);

    private final long logInterval;
    private final AtomicLong success = new AtomicLong();
    private final AtomicLong failure = new AtomicLong();
    private final AtomicLong error = new AtomicLong();
    private long nextLog = 0;

    public TrasierHttpClientHandler(final long logInterval) {
        this.logInterval = logInterval;
    }

    @Override
    public State onStatusReceived(HttpResponseStatus status) throws Exception {
        int statusCode = status.getStatusCode();
        if (statusCode >= 200 && statusCode < 300) {
            success.incrementAndGet();
        } else {
            failure.incrementAndGet();
        }
        return super.onStatusReceived(status);
    }

    @Override
    public Response onCompleted(Response response) throws Exception {
        long currentTime = System.currentTimeMillis();
        if (nextLog < currentTime) {
            synchronized (success) {
                synchronized (failure) {
                    synchronized (error) {
                        if (nextLog < currentTime) {
                            nextLog = currentTime + logInterval;
                            long success = this.success.getAndSet(0);
                            long failure = this.failure.getAndSet(0);
                            long error = this.error.getAndSet(0);
                            String metricsLog = "Trasier metrics (" + logInterval + "ms) - success: " + success + " - failure: " + failure + " - error: " + error;

                            if (error == 0 && failure == 0) {
                                LOGGER.info(metricsLog);
                            } else {
                                LOGGER.warn(metricsLog);
                            }
                        }
                    }
                }
            }
        }
        return super.onCompleted(response);
    }

    @Override
    public void onThrowable(Throwable t) {
        error.incrementAndGet();
        super.onThrowable(t);
    }
}
