package org.kevoree.core;

import org.KevoreeModel;
import org.KevoreeView;
import org.kevoree.Component;
import org.kevoree.Model;
import org.kevoree.Node;
import org.kevoree.api.callback.DeployCallback;
import org.kevoree.api.service.ModelService;
import org.kevoree.meta.MetaComponent;
import org.kevoree.meta.MetaModel;
import org.kevoree.meta.MetaNode;
import org.kevoree.modeling.KCallback;
import org.kevoree.modeling.KListener;
import org.kevoree.modeling.KObject;
import org.kevoree.modeling.KObjectIndex;
import org.kevoree.modeling.memory.manager.DataManagerBuilder;

import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;

public class KevoreeCore implements Runnable, ModelService {

    private KevoreeModel kModel;
    private Model model;
    private Node node;
    private final ScheduledExecutorService monoScheduler = Executors.newSingleThreadScheduledExecutor();

    public void boot(String url, final String nodeName) {
//        KContentDeliveryDriver cdd = new WebSocketPeer(url);
        kModel = new KevoreeModel(DataManagerBuilder.buildDefault());
        kModel.connect(new KCallback() {
            @Override
            public void on(Object o) {
                final KListener listener = kModel.createListener(0);
                kModel.manager().index(0, System.currentTimeMillis(), "root", new KCallback<KObjectIndex>() {
                    @Override
                    public void on(KObjectIndex index) {
                        if (index == null) {
                            model = kModel.createModel(0, System.currentTimeMillis());
                            kModel.save(null);
                        } else {
                            model = (Model) index;
                        }

                        model.traversal()
                                .traverse(MetaModel.REL_NODES)
                                .withAttribute(MetaComponent.ATT_NAME, nodeName)
                                .then(new KCallback<KObject[]>() {
                                    @Override
                                    public void on(KObject[] nodes) {
                                        if (nodes == null || nodes.length == 0) {
                                            node = kModel.createNode(0, System.currentTimeMillis());
                                            node.setName(nodeName);
                                            model.addNodes(node);
                                        } else {
                                            node = (Node) nodes[0];
                                        }
                                        kModel.save(null);
                                        listener.listen(node);
                                        listener.then(new KCallback() {
                                            @Override
                                            public void on(Object o) {
                                                monoScheduler.schedule(KevoreeCore.this, 0, TimeUnit.SECONDS);
                                            }
                                        });
                                    }
                                });
                    }
                });
            }
        });
    }

    @Override
    public void run() {
        //Here we check the consistency of the kModel according to
        if (node != null) {
            node.jump(System.currentTimeMillis(), new KCallback<KObject>() {
                @Override
                public void on(KObject kObject) {
                    Node newNode = (Node) kObject;
                    newNode.getComponents(new KCallback<Component[]>() {
                        @Override
                        public void on(Component[] components) {
                            for (final Component c : components) {
                                node.traversal()
                                        .traverse(MetaNode.REL_COMPONENTS)
                                        .withAttribute(MetaComponent.ATT_NAME, c.getName())
                                        .then(new KCallback<KObject[]>() {
                                            @Override
                                            public void on(KObject[] comps) {
                                                if (comps == null || comps.length == 0) {
                                                    System.out.println("new component: "+c.getName());
                                                } else {
                                                    // comp already exists
                                                }
                                            }
                                        });
                            }
                        }
                    });
                    newNode.getSubNodes(new KCallback<Node[]>() {
                        @Override
                        public void on(Node[] subNodes) {
                            for (final Node n : subNodes) {
                                node.traversal()
                                        .traverse(MetaNode.REL_SUBNODES)
                                        .withAttribute(MetaNode.ATT_NAME, n.getName())
                                        .then(new KCallback<KObject[]>() {
                                            @Override
                                            public void on(KObject[] nodes) {
                                                if (nodes == null || nodes.length == 0) {
                                                    System.out.println("new subNode: " + n.getName());
                                                } else {
                                                    // comp already exists
                                                }
                                            }
                                        });
                            }
                        }
                    });
                    node = newNode;
                }
            });
        }
    }

    @Override
    public Model getModel() {
        return model;
    }

    @Override
    public void deploy(Model model, DeployCallback callback) {
        kModel.connect(new KCallback() {
            @Override
            public void on(Object o) {
                KevoreeView kView = kModel.universe(0).time(System.currentTimeMillis());
            }
        });
    }

    public static void main(String[] args) {
        KevoreeCore core = new KevoreeCore();
        core.boot("ws://localhost:3080", "node0");
    }
}
