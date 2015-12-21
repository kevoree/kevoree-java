package org.kevoree.adaptation.business.operation;

import org.kevoree.DeployUnit;
import org.kevoree.adaptation.business.functional.Function;
import org.kevoree.adaptation.operation.AddDeployUnit;
import org.kevoree.adaptation.operation.util.AdaptationOperation;

/**
 * Created by mleduc on 21/12/15.
 */
public class AddDeployUnitOperation implements Function<DeployUnit, AdaptationOperation> {
    @Override
    public AdaptationOperation apply(DeployUnit du) {
        return new AddDeployUnit(du);
    }
}
