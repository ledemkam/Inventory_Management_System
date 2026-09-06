package com.kte.backend.annotation;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * Marks a method (or every public method of a type) whose execution time should be
 * measured and logged by {@link com.kte.backend.aspect.LoggingAspect}.
 * <p>
 * Use it to spot-check a specific operation without turning on debug logging for the
 * whole service layer.
 */
@Target({ElementType.METHOD, ElementType.TYPE})
@Retention(RetentionPolicy.RUNTIME)
public @interface LogExecutionTime {
}
