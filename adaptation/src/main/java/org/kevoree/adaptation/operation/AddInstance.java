package org.kevoree.adaptation.operation;

import org.kevoree.adaptation.operation.util.AdaptationOperation;
import org.kevoree.adaptation.operation.util.OperationOrder;

/**
 * Created by mleduc on 16/12/15.
 */
public class AddInstance extends AdaptationOperation {
    public final long uuid;

    public AddInstance(long uuid) {
        this.uuid = uuid;
    }

    @Override
    public OperationOrder getOperationOrder() {
        return OperationOrder.ADD_INSTANCE;
    }

    @Override
    public String toString() {
        return "AddInstance{" +
                "uuid=" + uuid +
                '}';
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;

        AddInstance that = (AddInstance) o;

        return uuid == that.uuid;

    }

    @Override
    public int hashCode() {
        return (int) (uuid ^ (uuid >>> 32));
    }
}
