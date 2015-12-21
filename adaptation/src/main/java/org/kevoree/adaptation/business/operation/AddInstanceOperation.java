package org.kevoree.adaptation.business.operation;

import org.kevoree.Instance;
import org.kevoree.adaptation.business.functional.Function;
import org.kevoree.adaptation.operation.AddInstance;
import org.kevoree.adaptation.operation.util.AdaptationOperation;

/**
 * Created by mleduc on 21/12/15.
 */
public class AddInstanceOperation<T extends Instance> implements Function<T, AdaptationOperation> {
    @Override
    public AdaptationOperation apply(T o) {
        return new AddInstance(o);
    }
}
