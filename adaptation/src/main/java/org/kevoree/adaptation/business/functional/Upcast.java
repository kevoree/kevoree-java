package org.kevoree.adaptation.business.functional;

import rx.functions.Func1;

/**
 * Created by mleduc on 21/12/15.
 */
public class Upcast<T> implements Func1<T, T> {
    @Override
    public T call(T inputPort) {
        return inputPort;
    }
}
