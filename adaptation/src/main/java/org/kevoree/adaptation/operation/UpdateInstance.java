package org.kevoree.adaptation.operation;

import org.kevoree.Instance;
import org.kevoree.adaptation.operation.util.AdaptationOperation;
import org.kevoree.adaptation.operation.util.OperationOrder;

/**
 * Update Instance Operation.
 * Created by mleduc on 16/12/15.
 */
public class UpdateInstance extends AdaptationOperation {
    public UpdateInstance(final Instance instance) {
        super(instance.uuid());
    }

    @Override
    public OperationOrder getOperationOrder() {
        return OperationOrder.UPDATE_INSTANCE;
    }

    @Override
    public String toString() {
        return "UpdateInstance{" +
                "uuid=" + uuid +
                '}';
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;

        UpdateInstance that = (UpdateInstance) o;

        return uuid == that.uuid;

    }

    @Override
    public int hashCode() {
        return (int) (uuid ^ (uuid >>> 32));
    }
}
