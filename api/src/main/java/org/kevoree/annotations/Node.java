package org.kevoree.annotations;

import java.lang.annotation.*;

/**
 *
 * Created by mleduc on 19/11/15.
 */
@Retention(RetentionPolicy.RUNTIME)
@Target({ElementType.TYPE})
@Documented
public @interface Node {
    String description() default "";
    int version();
}
