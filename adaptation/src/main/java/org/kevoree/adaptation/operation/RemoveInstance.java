package org.kevoree.adaptation.operation;

import org.kevoree.adaptation.operation.util.AdaptationOperation;
import org.kevoree.adaptation.operation.util.OperationOrder;

/**
 * Created by mleduc on 16/12/15.
 */
public class RemoveInstance extends AdaptationOperation {
    public final long uuid;

    public RemoveInstance(long uuid) {
        this.uuid = uuid;
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
    public int compareTo(AdaptationOperation o) {
        return super.compareTo(o);
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
