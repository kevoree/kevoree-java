package org.kevoree.adaptation.util;

import org.kevoree.Group;
import org.kevoree.adaptation.observable.util.ObservableDispatcher;

/**
 * Created by mleduc on 16/12/15.
 */
public interface Consumer<T> {
    void accept(T group);
}
