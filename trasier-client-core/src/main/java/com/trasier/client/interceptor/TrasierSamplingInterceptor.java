package com.trasier.client.interceptor;

import com.trasier.client.api.Span;

import java.util.Map;

public interface TrasierSamplingInterceptor {

    /**
     * Should the conversation be sampled or not.
     * @return <code>true</code> (default) if the conversation should be sampled, <code>false</code> otherwise
     */
    boolean shouldSample(Span span, Map<String, Object> params);

}
