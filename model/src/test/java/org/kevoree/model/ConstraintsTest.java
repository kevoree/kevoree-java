package org.kevoree.model;

import org.KevoreeModel;
import org.KevoreeView;
import org.kevoree.*;
import org.kevoree.modeling.KCallback;
import org.kevoree.modeling.memory.manager.DataManagerBuilder;

/**
 *
 * Created by mleduc on 07/12/15.
 */
public class ConstraintsTest {

    private KevoreeModel kModel = new KevoreeModel(DataManagerBuilder.buildDefault());

    /**
     * Defines a parameter of type Long that must have a value between 1 and 5.
     */
    public void testMinMax() {
        kModel.connect(new KCallback() {
            @Override
            public void on(Object o) {
                KevoreeView kView = kModel.universe(0).time(System.currentTimeMillis());

                final ParamType paramType = kView.createNumberParamType();

                final MinConstraint minConstraint = kView.createMinConstraint();
                minConstraint.setValue(1);

                final MaxConstraint maxConstraint = kView.createMaxConstraint();
                minConstraint.setValue(5);

                paramType.addConstraints(minConstraint)
                        .addConstraints(maxConstraint);
            }
        });
    }
}
