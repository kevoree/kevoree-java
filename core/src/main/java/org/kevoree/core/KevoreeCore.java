package org.kevoree.core;

import org.KevoreeModel;
import org.kevoree.Component;
import org.kevoree.Model;
import org.kevoree.Node;
import org.kevoree.core.api.Command;
import org.kevoree.core.api.Core;
import org.kevoree.meta.MetaComponent;
import org.kevoree.meta.MetaModel;
import org.kevoree.meta.MetaNode;
import org.kevoree.modeling.KListener;
import org.kevoree.modeling.KObject;
import org.kevoree.modeling.memory.manager.DataManagerBuilder;

import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;

public class KevoreeCore implements Core, Runnable {

    private KevoreeModel kModel;
    private Model model;
    private Node node;
    private final ScheduledExecutorService monoScheduler = Executors.newSingleThreadScheduledExecutor();

    public void boot(String url, String nodeName) {
        kModel = new KevoreeModel(DataManagerBuilder.create().withContentDeliveryDriver(new org.kevoree.modeling.drivers.websocket.WebSocketPeer(url)).build());
        kModel.connect(o -> {
            KListener listener = kModel.createListener(0);
            kModel.manager().getRoot(0, System.currentTimeMillis(), potentialRoot -> {
                if (potentialRoot == null) {
                    potentialRoot = kModel.createModel(0, System.currentTimeMillis());
                    model = (Model) potentialRoot;
                    kModel.manager().setRoot(potentialRoot, throwable -> {
                        if (throwable != null) {
                            throwable.printStackTrace();
                        }
                        kModel.save(null);
                    });
                } else {
                    model = (Model) potentialRoot;
                }

                model.traversal()
                        .traverse(MetaModel.REL_NODES)
                        .withAttribute(MetaComponent.ATT_NAME, nodeName)
                        .then(nodes -> {
                            if (nodes == null || nodes.length == 0) {
                                this.node = kModel.createNode(0, System.currentTimeMillis());
                                this.node.setName(nodeName);
                                model.addNodes(this.node);
                            } else {
                                this.node = (Node) nodes[0];
                            }
                            kModel.save(null);
                            listener.listen(this.node);
                            listener.then(o1 -> monoScheduler.schedule(this, 0, TimeUnit.SECONDS));
                        });
            });
        });
    }

    @Override
    public Model model() {
        return model;
    }

    @Override
    public Node node() {
        return node;
    }

    @Override
    public void run() {
        //Here we check the consistency of the kModel according to
        Node currentNode = node();
        if (currentNode != null) {
            currentNode.jump(System.currentTimeMillis(), (KObject currentNodeUncasted) -> {
                Node newNode = (Node) currentNodeUncasted;
                newNode.getComponents(components -> {
                    for (Component c : components) {
                        currentNode.traversal()
                                .traverse(MetaNode.REL_COMPONENTS)
                                .withAttribute(MetaComponent.ATT_NAME, c.getName())
                                .then(comps -> {
                                    if (comps == null || comps.length == 0) {
                                        System.out.println("new component: "+c.getName());
                                    } else {
                                        // comp already exists
                                    }
                                });
                    }
                });
                newNode.getSubNodes(subNodes -> {
                    for (Node n : subNodes) {
                        currentNode.traversal()
                                .traverse(MetaNode.REL_SUBNODES)
                                .withAttribute(MetaNode.ATT_NAME, n.getName())
                                .then(nodes -> {
                                    if (nodes == null || nodes.length == 0) {
                                        System.out.println("new subNode: " + n.getName());
                                    } else {
                                        // comp already exists
                                    }
                                });
                    }
                });
                node = newNode;
            });
        }
    }
}
