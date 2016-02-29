package org.kevoree.adaptation.operation;

import org.kevoree.Instance;
import org.kevoree.adaptation.operation.util.AdaptationOperation;
import org.kevoree.adaptation.operation.util.OperationOrder;
import org.kevoree.modeling.KObject;

/**
 * Created by mleduc on 18/12/15.
 */
public class StopInstance extends AdaptationOperation {
    public StopInstance(Instance instance) {
        super(instance.uuid());
    }

    @Override
    public OperationOrder getOperationOrder() {
        return OperationOrder.STOP_INSTANCE;
    }

    @Override
    public String toString() {
        return "StopInstance{" +
                "uuid=" + uuid +
                '}';
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;

        StopInstance that = (StopInstance) o;

        return uuid == that.uuid;

    }

    @Override
    public int hashCode() {
        return (int) (uuid ^ (uuid >>> 32));
    }
}
