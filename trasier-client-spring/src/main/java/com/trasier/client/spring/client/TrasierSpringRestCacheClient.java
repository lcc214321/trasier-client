package com.trasier.client.spring.client;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.LinkedBlockingDeque;
import java.util.concurrent.ScheduledThreadPoolExecutor;
import java.util.concurrent.SynchronousQueue;
import java.util.concurrent.ThreadPoolExecutor;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicInteger;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Primary;
import org.springframework.stereotype.Component;

import com.trasier.client.api.Span;
import com.trasier.client.configuration.TrasierClientConfiguration;
import com.trasier.client.spring.TrasierSpringConfiguration;

@Primary
@Component("trasierSpringCacheClient")
public class TrasierSpringRestCacheClient implements TrasierSpringClient, Runnable {
    private static final Logger LOGGER = LoggerFactory.getLogger(TrasierSpringRestCacheClient.class);

    private final TrasierClientConfiguration clientConfig;
    private final TrasierSpringConfiguration springConfiguration;
    private final TrasierSpringRestClient springRestClient;
    private final AtomicInteger countFullQueueErrors = new AtomicInteger();

    private LinkedBlockingDeque<Span> spanQueue;
    private int countFullQueueErrorsThreshold;
    private ScheduledThreadPoolExecutor scheduler;
    private ThreadPoolExecutor executor;
    private TrasierCompressSpanInterceptor compressSpanInterceptor;

    @Autowired
    public TrasierSpringRestCacheClient(TrasierClientConfiguration clientConfig, TrasierSpringConfiguration springConfiguration, TrasierSpringRestClient springRestClient) {
        this.clientConfig = clientConfig;
        this.springConfiguration = springConfiguration;
        this.springRestClient = springRestClient;

        if (clientConfig.isActivated()) {
            this.compressSpanInterceptor = new TrasierCompressSpanInterceptor();
            this.spanQueue = new LinkedBlockingDeque<>(springConfiguration.getQueueSize());
            this.countFullQueueErrorsThreshold = springConfiguration.getQueueSize() * springConfiguration.getQueueSizeErrorThresholdMultiplicator();

            this.scheduler = new ScheduledThreadPoolExecutor(1);
            this.scheduler.scheduleWithFixedDelay(this, springConfiguration.getQueueDelay(), springConfiguration.getQueueDelay(), TimeUnit.MILLISECONDS);

            this.executor = new ThreadPoolExecutor(1, 10, 60L, TimeUnit.SECONDS, new SynchronousQueue<>());
        }
    }

    @Override
    public boolean sendSpan(String accountId, String spaceKey, Span span) {
        return sendSpan(span);
    }

    @Override
    public boolean sendSpans(String accountId, String spaceKey, List<Span> spans) {
        return sendSpans(spans);
    }

    @Override
    public boolean sendSpan(Span span) {
        if (!clientConfig.isActivated()) {
            return false;
        }
        if (span.isCancel()) {
            return false;
        }
        try {
            compressSpanInterceptor.intercept(span);
            spanQueue.addLast(span);
            return true;
        } catch (IllegalStateException e) {
            if (countFullQueueErrors.incrementAndGet() == countFullQueueErrorsThreshold) {
                countFullQueueErrors.set(0);
                LOGGER.error("Queue full", e);
            } else {
                LOGGER.trace("Queue full", e);
            }
            return false;
        }
    }

    @Override
    public boolean sendSpans(List<Span> spans) {
        spans.forEach(this::sendSpan);
        return true;
    }

    @Override
    public void close() {
        try {
            scheduler.shutdown();
        } catch (Exception e) {
            LOGGER.error(e.getMessage(), e);
        }

        try {
            executor.shutdown();
        } catch (Exception e) {
            LOGGER.error(e.getMessage(), e);
        }
    }

    @Override
    public void run() {
        long maxNewTasks = springConfiguration.getMaxTaskCount() - executor.getTaskCount();
        int spanCount;

        do {
            final List<Span> spans = new ArrayList<>(springConfiguration.getMaxSpansPerTask());
            spanCount = spanQueue.drainTo(spans, springConfiguration.getMaxSpansPerTask());
            if(spanCount > 0) {
                executor.submit(() -> springRestClient.sendSpans(spans));
            }
        } while(spanCount == springConfiguration.getMaxSpansPerTask() && (maxNewTasks--) > 0);
    }

    public AtomicInteger getCountFullQueueErrors() {
        return countFullQueueErrors;
    }
}
