package org.kevoree.core;

import org.KevoreeModel;
import org.KevoreeUniverse;
import org.KevoreeView;
import org.kevoree.*;
import org.kevoree.modeling.KCallback;
import org.kevoree.modeling.memory.manager.DataManagerBuilder;

import java.util.Random;

/**
 *
 * Created by duke on 26/10/15.
 */
public class App {

    public static void main(String[] args) {
//        Random random = new Random();
//        KevoreeCore core = new KevoreeCore();
//        core.boot("ws://localhost:3080/shared", "node_" + Math.abs(random.nextInt()));

        KevoreeModel kModel = new KevoreeModel(DataManagerBuilder.buildDefault());
        kModel.connect(o -> {
            KevoreeUniverse kUniverse = kModel.universe(0);
            KevoreeView kView = kUniverse.time(System.currentTimeMillis());
            Model model = kView.createModel();

            Namespace ns = kView.createNamespace();
            ns.setName("kevoree");

            model.addNamespaces(ns);

            NodeType javaNode = kView.createNodeType();
            javaNode.setName("JavaNode");
            javaNode.setVersion("1");

            DeployUnit du = kView.createDeployUnit();
            du.setName("kevoree-java-node-java");
            du.setPlatform("java");
            du.setVersion("6.0.0-SNAPSHOT");

            javaNode.addDeployUnits(du);

            ns.addTypeDefinitions(javaNode);
        });
    }
}
