package org.kevoree.adaptation.util.functional;

import org.kevoree.Group;
import org.kevoree.adaptation.operation.util.AdaptationOperation;

/**
 * A function which takes an element of type T and return an element of type U.
 * Created by mleduc on 16/12/15.
 */
public interface Function<T, U> {
    U apply(T x);
}
