package org.kevoree.adaptation.operation;

import org.kevoree.Param;
import org.kevoree.adaptation.operation.util.AdaptationOperation;
import org.kevoree.adaptation.operation.util.OperationOrder;

/**
 * Update Instance Operation.
 * Created by mleduc on 16/12/15.
 */
public class UpdateParam extends AdaptationOperation {
    public UpdateParam(final Param param) {
        super(param.uuid());
    }

    @Override
    public OperationOrder getOperationOrder() {
        return OperationOrder.UPDATE_PARAM;
    }

    @Override
    public String toString() {
        return "UpdateParam{" +
                "uuid=" + uuid +
                '}';
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;

        UpdateParam that = (UpdateParam) o;

        return uuid == that.uuid;

    }

    @Override
    public int hashCode() {
        return (int) (uuid ^ (uuid >>> 32));
    }
}
