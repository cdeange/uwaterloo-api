package com.deange.uwaterlooapi.sample.dagger.scoping;

import java.lang.annotation.Documented;
import java.lang.annotation.Retention;
import javax.inject.Scope;

import static java.lang.annotation.RetentionPolicy.RUNTIME;

/**
 * Identifies a type that the injector only instantiates once per scope. Not inherited.
 *
 * @see Scope @Scope
 */
@Scope
@Documented
@Retention(RUNTIME)
public @interface SingleIn {
  Class<?> value();
}
