package org.kevoree.model;

import org.KevoreeModel;
import org.KevoreeUniverse;
import org.KevoreeView;
import org.kevoree.*;
import org.kevoree.modeling.memory.manager.DataManagerBuilder;

/**
 * Created by mleduc on 07/12/15.
 */
public class ConstraintsTest {


    /**
     * Defines a parameter of type Choix, which is Required.
     */
    public void testRequired() {
        final KevoreeModel kModel = new KevoreeModel(DataManagerBuilder.buildDefault());
        kModel.connect(o -> {
            System.out.println(o);
            KevoreeUniverse kUniverse = kModel.universe(0);
            KevoreeView kView = kUniverse.time(System.currentTimeMillis());
            final AttributeType attributeType = kView.createAttributeType();
            attributeType.addDatatype(kView.createChoiceDataType());
            attributeType.addConstraints(kView.createRequiredConstraint());
        });
    }

    /**
     * Defines a parameter of type Long, which is Required and must have a value between 1 and 5.
     */
    public void testNumericalValue() {
        final KevoreeModel kModel = new KevoreeModel(DataManagerBuilder.buildDefault());
        kModel.connect(o -> {
            System.out.println(o);
            KevoreeUniverse kUniverse = kModel.universe(0);
            KevoreeView kView = kUniverse.time(System.currentTimeMillis());
            final AttributeType attributeType = kView.createAttributeType();
            attributeType.addDatatype(kView.createLongDataType());
            final AndConstraint andConstraint = kView.createAndConstraint();
            final MinConstraint minConstraint = kView.createMinConstraint();
            minConstraint.setLimit(1);
            andConstraint.addConditions(minConstraint);

            final MaxConstraint maxConstraint = kView.createMaxConstraint();
            minConstraint.setLimit(5);
            andConstraint.addConditions(maxConstraint);
            andConstraint.addConditions(kView.createRequiredConstraint());

            attributeType.addConstraints(andConstraint);
        });
    }

    /**
     * Defines a parameter of type String, which is not Required and if defined must have a string of length between 1 and 7
     * or match specified regex.
     */
    public void testString() {
        final KevoreeModel kModel = new KevoreeModel(DataManagerBuilder.buildDefault());
        kModel.connect(o -> {
            System.out.println(o);
            KevoreeUniverse kUniverse = kModel.universe(0);
            KevoreeView kView = kUniverse.time(System.currentTimeMillis());

            final AttributeType attributeType = kView.createAttributeType();
            attributeType.addDatatype(kView.createStringDataType());

            final OrConstraint orConstraint = kView.createOrConstraint();
            final AndConstraint andConstraint1 = kView.createAndConstraint();
            andConstraint1.addConditions(kView.createMinLengthConstraint().setLimit(1));
            andConstraint1.addConditions(kView.createMaxLengthConstraint().setLimit(7));
            orConstraint.addConditions(andConstraint1);

            orConstraint.addConditions(kView.createPatternContraint().setRegex("[0-3]{11,14}"));

            attributeType.addConstraints(orConstraint);
        });
    }
}
