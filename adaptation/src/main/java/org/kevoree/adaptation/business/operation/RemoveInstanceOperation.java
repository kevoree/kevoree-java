package org.kevoree.adaptation.business.operation;

import org.kevoree.Instance;
import org.kevoree.adaptation.business.functional.Function;
import org.kevoree.adaptation.operation.RemoveInstance;
import org.kevoree.adaptation.operation.util.AdaptationOperation;

/**
 * Created by mleduc on 21/12/15.
 */
public class RemoveInstanceOperation<T extends Instance> implements Function<T, AdaptationOperation> {
    @Override
    public AdaptationOperation apply(T o) {
        return new RemoveInstance(o);
    }
}
