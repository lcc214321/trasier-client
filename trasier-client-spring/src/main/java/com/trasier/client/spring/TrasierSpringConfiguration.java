package com.trasier.client.spring;

import org.springframework.context.annotation.ComponentScan;
import org.springframework.context.annotation.Configuration;

import com.trasier.client.spring.auth.OAuthToken;
import com.trasier.client.spring.client.TrasierSpringClient;
import com.trasier.client.spring.context.TrasierSpringAccessor;
import com.trasier.client.spring.spancontrol.TrasierSampleByOperationInterceptor;

@Configuration
@ComponentScan(basePackageClasses = {OAuthToken.class, TrasierSpringClient.class, TrasierSpringAccessor.class, TrasierSampleByOperationInterceptor.class})
public class TrasierSpringConfiguration {
    private int queueSize = 1000;
    private int queueSizeErrorThresholdMultiplicator = 10;
    private long queueDelay = 500L;
    private int maxTaskCount = 100;
    private int maxSpansPerTask = 10;

    public int getQueueSize() {
        return queueSize;
    }

    public void setQueueSize(int queueSize) {
        this.queueSize = queueSize;
    }

    public long getQueueDelay() {
        return queueDelay;
    }

    public void setQueueDelay(long queueDelay) {
        this.queueDelay = queueDelay;
    }

    public int getMaxTaskCount() {
        return maxTaskCount;
    }

    public void setMaxTaskCount(int maxTaskCount) {
        this.maxTaskCount = maxTaskCount;
    }

    public int getMaxSpansPerTask() {
        return maxSpansPerTask;
    }

    public void setMaxSpansPerTask(int maxSpansPerTask) {
        this.maxSpansPerTask = maxSpansPerTask;
    }

    public int getQueueSizeErrorThresholdMultiplicator() {
        return queueSizeErrorThresholdMultiplicator;
    }

    public void setQueueSizeErrorThresholdMultiplicator(int queueSizeErrorThresholdMultiplicator) {
        this.queueSizeErrorThresholdMultiplicator = queueSizeErrorThresholdMultiplicator;
    }
}