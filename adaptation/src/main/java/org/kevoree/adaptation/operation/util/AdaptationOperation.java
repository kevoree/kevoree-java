package org.kevoree.adaptation.operation.util;

/**
 * Created by mleduc on 16/12/15.
 */
public abstract class AdaptationOperation implements Comparable<AdaptationOperation> {

    @Override
    public int compareTo(AdaptationOperation o) {
        return this.getOperationOrder().compareTo(o.getOperationOrder());
    }

    public abstract OperationOrder getOperationOrder();
}
