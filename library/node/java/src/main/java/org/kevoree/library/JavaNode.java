package org.kevoree.library;

import org.kevoree.annotations.Node;
import org.kevoree.annotations.params.ChoiceParam;
import org.kevoree.api.AdaptationPrimitive;
import org.kevoree.api.NodeInstance;
import org.kevoree.library.adaptation.AddDeployUnit;
import org.kevoree.library.adaptation.AddInstance;
import org.kevoree.library.adaptation.RemoveDeployUnit;
import org.kevoree.library.adaptation.RemoveInstance;

/**
 *
 * Created by mleduc on 23/11/15.
 */
@Node("JavaNode platform")
public class JavaNode implements NodeInstance {

    @ChoiceParam
    private LogLevel logLevel = LogLevel.INFO;

    private final AdaptationPrimitive addInstance = new AddInstance();
    private final AdaptationPrimitive removeInstance = new RemoveInstance();
    private final AdaptationPrimitive addDeployUnit = new AddDeployUnit();
    private final AdaptationPrimitive removeDeployUnit = new RemoveDeployUnit();

    @Override
    public AdaptationPrimitive getAddInstance() {
        return addInstance;
    }

    @Override
    public AdaptationPrimitive getRemoveInstance() {
        return removeInstance;
    }

    @Override
    public AdaptationPrimitive getAddDeployUnit() {
        return addDeployUnit;
    }

    @Override
    public AdaptationPrimitive getRemoveDeployUnit() {
        return removeDeployUnit;
    }
}
