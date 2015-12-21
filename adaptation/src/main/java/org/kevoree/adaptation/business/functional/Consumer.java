package org.kevoree.adaptation.business.functional;

/**
 * Equivalent to a call to a method with one paramter a void retur type.
 * Created by mleduc on 16/12/15.
 */
public interface Consumer<T> {
    void accept(T group);
}
