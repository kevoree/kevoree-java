package org.kevoree.adaptation.operation;

import org.kevoree.Instance;
import org.kevoree.Port;
import org.kevoree.adaptation.operation.util.AdaptationOperation;
import org.kevoree.adaptation.operation.util.OperationOrder;

/**
 * Created by mleduc on 18/12/15.
 */
public class AddBinding extends AdaptationOperation{
    public AddBinding(final Port instance) {
        super(instance.uuid());
    }

    @Override
    public OperationOrder getOperationOrder() {
        return OperationOrder.ADD_BINDING;
    }

    @Override
    public String toString() {
        return "AddBinding{" +
                "uuid=" + uuid +
                '}';
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;

        AddBinding that = (AddBinding) o;

        return uuid == that.uuid;

    }

    @Override
    public int hashCode() {
        return (int) (uuid ^ (uuid >>> 32));
    }
}
