/*
 * Copyright (C) Schweizerische Bundesbahnen SBB, 2020.
 */
package com.trasier.client.spring.spancontrol;

import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.NonNull;

import java.util.List;
import java.util.regex.Pattern;

/**
 * Sample configuration
 * <pre>
trasier:
    client:
        span:
            filters:
            -
               strategy: cancel
               url: .*admin.*|*.payment.*
               operation: *.checkPayment.*
            -
               strategy: allow
               url: .*offer.*
               operation: *.booking.*
            -
               strategy: disablePayload
               url: .*login.*|*.payment.*
               operation: *.checkPayment.*|userLogin
 </code>
 */
@Data
public class TrasierSpanFilterConfiguration {

    private List<SpanFilter> filters;

}

@Data
@NoArgsConstructor
class SpanFilter {

    @NonNull
    private Strategy strategy;
    private Pattern operation;
    private Pattern url;

    public void setUrl(String urlPattern) {
        this.url = Pattern.compile(urlPattern);
    }
    public void setOperation(String operationPattern) {
        this.operation = Pattern.compile(operationPattern);
    }
}

enum Strategy {
    cancel, allow, disablePayload
}
