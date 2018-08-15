package com.trasier.client.impl.spring.sleuth;

import org.springframework.context.annotation.Import;

import java.lang.annotation.Documented;
import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

@Target(ElementType.TYPE)
@Retention(RetentionPolicy.RUNTIME)
@Import({TrasierSleuthConfiguration.class})
@Documented
public @interface EnableTrasierSleuth {
}
