package org.kevoree.adaptation.business.operation;

import org.kevoree.DeployUnit;
import org.kevoree.adaptation.business.functional.Function;
import org.kevoree.adaptation.operation.RemoveDeployUnit;
import org.kevoree.adaptation.operation.util.AdaptationOperation;

/**
 * Created by mleduc on 21/12/15.
 */
public class RemoveDeployUnitOperation implements Function<DeployUnit, AdaptationOperation> {
    @Override
    public AdaptationOperation apply(DeployUnit du) {
        return new RemoveDeployUnit(du);
    }
}
