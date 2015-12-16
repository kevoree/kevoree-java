package org.kevoree.adaptation.util;

/**
 * Created by mleduc on 14/12/15.
 */
public interface PredicateFactory<T> {
    Predicate<? super T> get(T a);
}
