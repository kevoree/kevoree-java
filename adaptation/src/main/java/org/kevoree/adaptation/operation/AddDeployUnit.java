package org.kevoree.adaptation.operation;

import org.kevoree.DeployUnit;
import org.kevoree.Port;
import org.kevoree.adaptation.operation.util.AdaptationOperation;
import org.kevoree.adaptation.operation.util.OperationOrder;

/**
 * Created by mleduc on 18/12/15.
 */
public class AddDeployUnit extends AdaptationOperation{
    public AddDeployUnit(final DeployUnit du) {
        super(du.uuid());
    }

    @Override
    public OperationOrder getOperationOrder() {
        return OperationOrder.ADD_DEPLOY_UNIT;
    }

    @Override
    public String toString() {
        return "AddDeployUnit{" +
                "uuid=" + uuid +
                '}';
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;

        AddDeployUnit that = (AddDeployUnit) o;

        return uuid == that.uuid;

    }

    @Override
    public int hashCode() {
        return (int) (uuid ^ (uuid >>> 32));
    }
}
