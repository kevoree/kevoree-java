package org.kevoree.adaptation.operation;

import org.kevoree.adaptation.operation.util.AdaptationOperation;
import org.kevoree.adaptation.operation.util.OperationOrder;

/**
 * Remove instance Operation.
 * Created by mleduc on 16/12/15.
 */
public class RemoveInstance extends AdaptationOperation {

    public RemoveInstance(long uuid) {
        super(uuid);
    }

    @Override
    public OperationOrder getOperationOrder() {
        return OperationOrder.REMOVE_INSTANCE;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;

        RemoveInstance that = (RemoveInstance) o;

        return uuid == that.uuid;

    }

    @Override
    public int compareTo(AdaptationOperation adaptationOperation) {
        return super.compareTo(adaptationOperation);
    }

    @Override
    public String toString() {
        return "RemoveInstance{" +
                "uuid=" + uuid +
                '}';
    }

    @Override
    public int hashCode() {
        return (int) (uuid ^ (uuid >>> 32));
    }
}
