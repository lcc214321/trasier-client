/*
 * Copyright (C) Schweizerische Bundesbahnen SBB, 2020.
 */
package com.trasier.client.configuration;

import lombok.Data;

import java.util.regex.Pattern;

@Data
public class TrasierFilterConfiguration {

    private Pattern url;
    private Pattern operation;

    public void setUrl(String urlPattern) {
        this.url = Pattern.compile(urlPattern);
    }

    public void setOperation(String operationPattern) {
        this.operation = Pattern.compile(operationPattern);
    }

}

