package org.kevoree.model;

import org.KevoreeModel;
import org.KevoreeView;
import org.kevoree.*;
import org.kevoree.meta.MetaNumberType;
import org.kevoree.modeling.KCallback;
import org.kevoree.modeling.memory.manager.DataManagerBuilder;

/**
 *
 * Created by leiko on 12/2/15.
 */
public class TestDictionaryType {


    /**
     * This block of code is here for compilation time test.
     * During a meta-model development iteration you can check if it still compile and assert that you do not have created unanticipated regressions.
     */
    public void test() {
        final KevoreeModel kModel = new KevoreeModel(DataManagerBuilder.buildDefault());
        kModel.connect(new KCallback() {
            @Override
            public void on(Object o) {
                KevoreeView kView = kModel.universe(0).time(System.currentTimeMillis());

                DictionaryType dType = kView.createDictionaryType();
                NumberParamType pType = kView.createNumberParamType();
                pType.addConstraints(kView.createMinConstraint().setValue(1).setExclusive(false))
                        .addConstraints(kView.createMaxConstraint().setValue(5).setExclusive(false));
                pType.setDefault("1");
                pType.setType(MetaNumberType.INT);
                dType.addParams(pType);

                ComponentType compType = kView.createComponentType();
                compType.addDictionary(dType);

                Component comp = kView.createComponent();
                comp.setName("comp");
                comp.addTypeDefinition(compType);

                Dictionary dictionary = kView.createDictionary();
                NumberParam numberParam = kView.createNumberParam();
                numberParam.addType(pType);
                numberParam.setValue(pType.getDefault());

                dictionary.addParams(numberParam);
            }
        });
    }
}
