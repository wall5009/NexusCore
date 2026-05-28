package com.rollylindenshnizzer.nexuscore.api;

import java.lang.annotation.Documented;
import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

@Documented
@Retention(RetentionPolicy.RUNTIME)
@Target({ElementType.TYPE, ElementType.METHOD, ElementType.CONSTRUCTOR, ElementType.FIELD, ElementType.PACKAGE})
public @interface NexusIncubating {
    String since();

    String note() default "Incubating NexusCore API. Source compatibility is intended, but the shape may be refined during the current minor line.";
}
