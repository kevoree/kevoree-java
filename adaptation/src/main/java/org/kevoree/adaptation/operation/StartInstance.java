package org.kevoree.adaptation.operation;

import org.kevoree.Instance;
import org.kevoree.adaptation.operation.util.AdaptationOperation;
import org.kevoree.adaptation.operation.util.OperationOrder;

/**
 * Created by mleduc on 18/12/15.
 */
public class StartInstance extends AdaptationOperation {
    public StartInstance(Instance instance) {
        super(instance.uuid());
    }

    @Override
    public OperationOrder getOperationOrder() {
        return OperationOrder.START_INSTANCE;
    }

    @Override
    public String toString() {
        return "StartInstance{" +
                "uuid=" + uuid +
                '}';
    }
    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;

        StartInstance that = (StartInstance) o;

        return uuid == that.uuid;

    }

    @Override
    public int hashCode() {
        return (int) (uuid ^ (uuid >>> 32));
    }

}
