package org.kevoree.annotations.params;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * <strong>This annotation makes sense only inside {@code @Group} and
 * {@code @Channel}</strong>
 * <br/><br/>
 * It indicates that the param must be placed in a fragment dictionary,
 * which is created each time a group or a channel is connected to a node.
 */
@Retention(RetentionPolicy.RUNTIME)
@Target(ElementType.FIELD)
public @interface Fragment {
}
