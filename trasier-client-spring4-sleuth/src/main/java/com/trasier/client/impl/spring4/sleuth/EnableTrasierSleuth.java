package com.trasier.client.impl.spring4.sleuth;

import com.trasier.client.impl.spring4.TrasierSpringConfiguration;
import org.springframework.context.annotation.Import;

import java.lang.annotation.Documented;
import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

@Target(ElementType.TYPE)
@Retention(RetentionPolicy.RUNTIME)
@Import({TrasierSleuthConfiguration.class, TrasierSpringConfiguration.class})
@Documented
public @interface EnableTrasierSleuth {
}