package com.trasier.client.impl.spring4;

import com.trasier.client.impl.spring4.client.SpringClient;
import com.trasier.client.impl.spring4.client.SpringRestCacheClient;
import com.trasier.client.impl.spring4.client.SpringRestClient;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.ComponentScan;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Primary;
import org.springframework.context.support.PropertySourcesPlaceholderConfigurer;
import org.springframework.http.converter.json.MappingJackson2HttpMessageConverter;
import org.springframework.web.client.RestTemplate;

@Configuration
@ComponentScan(basePackageClasses = SpringRestClient.class)
public class TrasierSpringConfiguration {
    private int queueSize = 100;
    private long queueDelay = 1000L;
    private int maxTaskCount = 100;
    private int maxSpansPerTask = 10;

    @Bean
    public static PropertySourcesPlaceholderConfigurer propertySourcesPlaceholderConfigurer() {
        return new PropertySourcesPlaceholderConfigurer();
    }

    @Bean(name = "trasierRestTemplate")
    public RestTemplate createRestTemplate() {
        RestTemplate restTemplate = new RestTemplate();
        restTemplate.getMessageConverters().add(new MappingJackson2HttpMessageConverter());
        return restTemplate;
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
}