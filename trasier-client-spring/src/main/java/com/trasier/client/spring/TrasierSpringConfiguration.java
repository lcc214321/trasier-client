package com.trasier.client.spring;

import com.trasier.client.interceptor.DefaultTrasierSpanInterceptor;
import com.trasier.client.interceptor.TrasierInterceptorRegistry;
import com.trasier.client.spring.auth.OAuthToken;
import com.trasier.client.spring.client.TrasierSpringClient;
import com.trasier.client.spring.context.TrasierSpringAccessor;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.ComponentScan;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.support.PropertySourcesPlaceholderConfigurer;

@Configuration
@ComponentScan(basePackageClasses = { OAuthToken.class, TrasierSpringClient.class, TrasierSpringAccessor.class })
public class TrasierSpringConfiguration {
    private int queueSize = 100;
    private int queueSizeErrorThresholdMultiplicator = 10;
    private long queueDelay = 1000L;
    private int maxTaskCount = 100;
    private int maxSpansPerTask = 10;

    @Bean
    public static PropertySourcesPlaceholderConfigurer propertySourcesPlaceholderConfigurer() {
        return new PropertySourcesPlaceholderConfigurer();
    }

    @Bean
    public TrasierInterceptorRegistry interceptorRegistry() {
        TrasierInterceptorRegistry trasierInterceptorRegistry = new TrasierInterceptorRegistry();
        trasierInterceptorRegistry.addSpanInterceptor(new DefaultTrasierSpanInterceptor());
        return trasierInterceptorRegistry;
    }

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