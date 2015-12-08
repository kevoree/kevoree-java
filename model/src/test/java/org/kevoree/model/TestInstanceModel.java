package org.kevoree.model;

import org.KevoreeModel;
import org.KevoreeView;
import org.kevoree.*;
import org.kevoree.modeling.memory.manager.DataManagerBuilder;

/**
 *
 * Created by leiko on 12/2/15.
 */
public class TestInstanceModel {

    //@Test
    public void test() {
        KevoreeModel kModel = new KevoreeModel(DataManagerBuilder.buildDefault());
        kModel.connect(o -> {
//            RestGateway.expose(kModel, 8050).start();

            KevoreeView kView = kModel.universe(0).time(0);
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

            Group group = kView.createGroup();
            group.setName("group");
            group.addNodes(node0);
            model.addGroups(group);

            NodeType nodeType = kView.createNodeType();
            nodeType.setName("JavaNode");
            nodeType.setVersion("1");

            Namespace kevoreeNamespace = kView.createNamespace();
            kevoreeNamespace.setName("kevoree");
            kevoreeNamespace.addTypeDefinitions(nodeType);
            model.addNamespaces(kevoreeNamespace);

            kModel.save(null);
        });
    }

    public static void main(String[] args) {
        new TestInstanceModel().test();
    }
}
