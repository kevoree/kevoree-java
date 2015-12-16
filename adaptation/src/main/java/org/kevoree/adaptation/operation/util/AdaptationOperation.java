package org.kevoree.adaptation.operation.util;

/**
 * Abstract class of every adaptation operations.
 * <p>
 * Created by mleduc on 16/12/15.
 */
public abstract class AdaptationOperation implements Comparable<AdaptationOperation> {

    /**
     * Used to determine the order of execution of the adaptations.
     *
     * @param adaptationOperation Another adaptation operation.
     * @return
     */
    @Override
    public int compareTo(AdaptationOperation adaptationOperation) {
        return this.getOperationOrder().compareTo(adaptationOperation.getOperationOrder());
    }

    /**
     * @return the adaptation order of the operation.
     */
    public abstract OperationOrder getOperationOrder();
}
