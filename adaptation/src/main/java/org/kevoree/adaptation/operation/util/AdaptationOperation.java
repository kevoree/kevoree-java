package org.kevoree.adaptation.operation.util;

import java.util.Objects;

/**
 * Abstract class of every adaptation operations.
 * <p>
 * Created by mleduc on 16/12/15.
 */
public abstract class AdaptationOperation implements Comparable<AdaptationOperation> {

    protected final Long uuid;

    protected AdaptationOperation(Long uuid) {
        this.uuid = uuid;
    }

    /**
     * Used to determine the order of execution of the adaptations.
     *
     * @param adaptationOperation Another adaptation operation.
     * @return
     */
    @Override
    public int compareTo(AdaptationOperation adaptationOperation) {
        int comp0 = this.getOperationOrder().compareTo(adaptationOperation.getOperationOrder());
        if (comp0 != 0) {
            return comp0;
        }

        return this.uuid.compareTo(adaptationOperation.uuid);
    }


    /**
     * @return the adaptation order of the operation.
     */
    public abstract OperationOrder getOperationOrder();
}
