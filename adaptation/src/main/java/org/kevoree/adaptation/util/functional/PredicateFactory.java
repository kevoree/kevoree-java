package org.kevoree.adaptation.util.functional;

import org.kevoree.adaptation.util.functional.Predicate;

/**
 * Take an element of type T and retun a predicate of type T.
 * It is used to generate a predicate which is able to compare two elements of type T dynamically.
 * Created by mleduc on 14/12/15.
 */
public interface PredicateFactory<T> {
    Predicate<? super T> get(T a);
}
