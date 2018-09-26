/*
 * Copyright (C) Schweizerische Bundesbahnen SBB, 2018.
 */

package com.trasier.client.impl.spring.opentracing.boot;

import org.springframework.context.annotation.Import;

import java.lang.annotation.Documented;
import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

@Target({ElementType.TYPE})
@Retention(RetentionPolicy.RUNTIME)
@Documented
@Import({TrasierOpentracingConfig.class})
public @interface EnableTrasierOpentracing {
}
