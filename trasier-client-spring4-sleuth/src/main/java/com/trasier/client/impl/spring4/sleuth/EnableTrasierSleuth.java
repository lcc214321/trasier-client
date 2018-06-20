package com.trasier.client.impl.spring4.sleuth;

import org.springframework.context.annotation.Import;

import java.lang.annotation.*;

@Target(ElementType.TYPE)
@Retention(RetentionPolicy.RUNTIME)
@Import(TrasierSleuthConfiguration.class)
@Documented
public @interface EnableTrasierSleuth {
}