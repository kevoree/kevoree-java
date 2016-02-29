package org.kevoree.adaptation.business.operation;

import org.kevoree.Port;
import org.kevoree.adaptation.business.functional.Function;
import org.kevoree.adaptation.operation.RemoveBinding;
import org.kevoree.adaptation.operation.util.AdaptationOperation;

/**
 * Created by mleduc on 21/12/15.
 */
public class RemoveBindingOperation<T extends Port> implements Function<T, AdaptationOperation> {
    @Override
    public AdaptationOperation apply(T o) {
        return new RemoveBinding(o);
    }
}
