package org.kevoree.model;

import org.KevoreeModel;
import org.KevoreeUniverse;
import org.KevoreeView;
import org.junit.Test;
import org.kevoree.*;
import org.kevoree.modeling.memory.manager.DataManagerBuilder;

/**
 *
 * Created by leiko on 12/2/15.
 */
public class TestTypeModel {


    /**
     * This block of code is here for compilation time test.
     * During a meta-model development iteration you can check if it still compile and assert that you do not have created unanticipated regressions.
     */
    public void test() {
        KevoreeModel kModel = new KevoreeModel(DataManagerBuilder.buildDefault());
        kModel.connect(o -> {
            KevoreeUniverse kUniverse = kModel.universe(0);
            KevoreeView kView = kUniverse.time(System.currentTimeMillis());
            Model model = kView.createModel();

            Namespace kevoree = kView.createNamespace();
            kevoree.setName("kevoree");
            model.addNamespaces(kevoree);

            NodeType javaNode = kView.createNodeType();
            javaNode.setName("JavaNode");
            javaNode.setVersion("1");
            kevoree.addTypeDefinitions(javaNode);

            DeployUnit du = kView.createDeployUnit();
            du.setName("kevoree-java-node-java");
            du.setPlatform("java");
            du.setVersion("6.0.0-SNAPSHOT");
            javaNode.addDeployUnits(du);

            ComponentType ticker = kView.createComponentType();
            ticker.setName("Ticker");
            ticker.setVersion("1");
            kevoree.addTypeDefinitions(ticker);

            DeployUnit tickerDU = kView.createDeployUnit();
            tickerDU.setName("ticker");
            tickerDU.setPlatform("java");
            ticker.addDeployUnits(tickerDU);

            DictionaryType dic = kView.createDictionaryType();
            ticker.addDictionary(dic);

            AttributeType random = kView.createAttributeType();
            random.setOptional(false);
            dic.addAttributes(random);

            BooleanDataType randomType = kView.createBooleanDataType();
            randomType.setDefault(false);
            random.addDatatype(randomType);

            AttributeType period = kView.createAttributeType();
            period.setOptional(true);
            dic.addAttributes(period);

            IntDataType periodType = kView.createIntDataType();
            periodType.setDefault(3000);
            period.addDatatype(periodType);

            PortType tick = kView.createPortType();
            tick.setName("tick");
            ticker.addOutputTypes(tick);

            kView.json().save(model, modelStr -> {
                System.out.println(modelStr);
                kModel.disconnect(t -> {});
            });
        });
    }
}
