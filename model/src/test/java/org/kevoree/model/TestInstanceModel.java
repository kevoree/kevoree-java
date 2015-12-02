package org.kevoree.model;

import org.KevoreeModel;
import org.KevoreeUniverse;
import org.KevoreeView;
import org.junit.Test;
import org.kevoree.*;
import org.kevoree.meta.MetaComponent;
import org.kevoree.meta.MetaModel;
import org.kevoree.meta.MetaNode;
import org.kevoree.meta.MetaOutputPort;
import org.kevoree.modeling.KObject;
import org.kevoree.modeling.memory.manager.DataManagerBuilder;

/**
 * Created by leiko on 12/2/15.
 */
public class TestInstanceModel {

    @Test
    public void test() {
        KevoreeModel kModel = new KevoreeModel(DataManagerBuilder.buildDefault());
        kModel.connect(o -> {
            KevoreeUniverse kUniverse = kModel.universe(0);
            KevoreeView kView = kUniverse.time(System.currentTimeMillis());
            Model model = kView.createModel();

            Node node0 = kView.createNode();
            node0.setName("node0");
            model.addNodes(node0);

            Component ticker = kView.createComponent();
            ticker.setName("ticker");
            node0.addComponents(ticker);

            OutputPort tickPort = kView.createOutputPort();
            tickPort.setName("tick");
            ticker.addOutputs(tickPort);

            Channel chan = kView.createChannel();
            chan.setName("chan");
            chan.addOutputs(tickPort);
            tickPort.addChannels(chan);
            model.addChannels(chan);

            kView.json().save(model, modelStr -> {
                kView.json().load(modelStr, loadedModel -> {
                    System.out.println(loadedModel);
                    Model newModel = (Model) loadedModel;
                    newModel.traversal()
                            .traverse(MetaModel.REL_NODES)
                            .withAttribute(MetaNode.ATT_NAME, "node0")
                            .traverse(MetaNode.REL_COMPONENTS)
                            .withAttribute(MetaComponent.ATT_NAME, "ticker")
                            .traverse(MetaComponent.REL_OUTPUTS)
                            .withAttribute(MetaOutputPort.ATT_NAME, "tick")
                            .then(results -> {
                                for (KObject res : results) {
                                    System.out.println(res.toJSON());
                                }
                            });
                });
            });
        });
    }
}
