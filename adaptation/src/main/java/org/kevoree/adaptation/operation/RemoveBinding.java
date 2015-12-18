package org.kevoree.adaptation.operation;

import org.kevoree.Instance;
import org.kevoree.Port;
import org.kevoree.adaptation.operation.util.AdaptationOperation;
import org.kevoree.adaptation.operation.util.OperationOrder;

/**
 * Created by mleduc on 18/12/15.
 */
public class RemoveBinding extends AdaptationOperation {
    public RemoveBinding(final Port port) {
        super(port.uuid());
    }

    @Override
    public OperationOrder getOperationOrder() {
        return OperationOrder.REMOVE_BINDING;
    }

    @Override
    public String toString() {
        return "RemoveBinding{" +
                "uuid=" + uuid +
                '}';
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;

        RemoveBinding that = (RemoveBinding) o;

        return uuid == that.uuid;

    }

    @Override
    public int hashCode() {
        return (int) (uuid ^ (uuid >>> 32));
    }
}
