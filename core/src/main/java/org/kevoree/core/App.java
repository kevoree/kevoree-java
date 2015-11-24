package org.kevoree.core;

import org.KevoreeModel;
import org.KevoreeUniverse;
import org.KevoreeView;
import org.kevoree.*;
import org.kevoree.meta.MetaComponent;
import org.kevoree.meta.MetaModel;
import org.kevoree.meta.MetaNode;
import org.kevoree.meta.MetaOutputPort;
import org.kevoree.modeling.KObject;
import org.kevoree.modeling.memory.manager.DataManagerBuilder;

/**
 *
 * Created by duke on 26/10/15.
 */
public class App {

    public static void main(String[] args) {
//        Random random = new Random();
//        KevoreeCore core = new KevoreeCore();
//        core.boot("ws://localhost:3080/shared", "node_" + Math.abs(random.nextInt()));

        createTypeModel();
//        createInstanceModel();
    }

    private static void createInstanceModel() {
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

    private static void createTypeModel() {
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

            /*MsgProtocol tickProto = kView.createMsgProtocol();
            tickProto.setName("jsonschema");
            Value schema = kView.createValue();
            schema.setName("schema");
            schema.setValue("{\"type\": \"number\"}");
            tickProto.addMetaData(schema);
            tick.addProtocol(tickProto);
*/
            kView.json().save(model, modelStr -> {
                System.out.println(modelStr);
                kModel.disconnect(t -> {});
            });
        });
    }
}
