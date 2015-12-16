package org.kevoree.adaptation.util;

import org.kevoree.Group;
import org.kevoree.adaptation.operation.util.AdaptationOperation;

/**
 * Created by mleduc on 16/12/15.
 */
public interface Function<T, U> {
    U apply(T x);
}
