package org.kevoree.annotations.params;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 *
 * Created by leiko on 11/30/15.
 */
@Retention(RetentionPolicy.RUNTIME)
@Target(ElementType.FIELD)
public @interface IntParam {
    int min() default Integer.MIN_VALUE;
    int max() default Integer.MAX_VALUE;
}
