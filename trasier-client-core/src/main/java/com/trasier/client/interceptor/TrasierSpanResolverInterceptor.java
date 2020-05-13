package com.trasier.client.interceptor;

import com.trasier.client.api.Span;

public interface TrasierSpanResolverInterceptor {

    /**
     * Intercepts when the request url was resolved (but before span metadata was resolved)
     * @param span - raw span, not filled with metadata
     * @param url - request url
     */
    void interceptRequestUrlResolved(Span span, String url);

    /**
     * Resolved metadata like operation name, endpoints
     * @param span - span filled with metadata
     */
    void interceptMetdataResolved(Span span);

    /**
     * Invoked when the message payload was resolved.
     * At this point the metadata such as operation name are already resolved.
     * The message payload can be validated for sensitive information and one could mask the messag payload,
     * whipe it out or cancel the span.
     * Note that the message payload on the span may be null or empty.
     * @param span - span filled with metadata and payload
     */
    void interceptMessagePayloadResolved(Span span);

}
