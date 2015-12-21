package org.kevoree.adaptation.business.functional;

/**
 * Takes an element of type T and return a boolean.
 * Created by mleduc on 16/12/15.
 */
public interface Predicate<T> {
    boolean test(T b);
}
