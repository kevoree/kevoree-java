package org.kevoree.kernel;

import org.KevoreeModel;
import org.kevoree.Model;
import org.kevoree.Node;
import org.kevoree.meta.MetaInstance;
import org.kevoree.meta.MetaModel;
import org.kevoree.meta.MetaNode;
import org.kevoree.modeling.KObject;
import org.kevoree.modeling.memory.manager.DataManagerBuilder;

import java.lang.management.ManagementFactory;
import java.lang.management.OperatingSystemMXBean;
import java.util.Random;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;

public class KevoreeKernel {

    private KevoreeModel model;

    private Model root;

    private Node node;

    private ScheduledExecutorService scheduledExecutorService = Executors.newSingleThreadScheduledExecutor();

    public void boot(String url, String nodeName) {
        model = new KevoreeModel(DataManagerBuilder.create().withContentDeliveryDriver(new org.kevoree.modeling.drivers.websocket.WebSocketPeer(url)).build());
        model.connect(o -> {
            model.manager().getRoot(0, System.currentTimeMillis(), potentialRoot -> {
                if (potentialRoot == null) {
                    potentialRoot = model.createModel(0, System.currentTimeMillis());
                    root = (Model) potentialRoot;
                    model.manager().setRoot(potentialRoot, throwable -> {
                        if (throwable != null) {
                            throwable.printStackTrace();
                        }
                        model.save(null);
                    });
                } else {
                    root = (Model) potentialRoot;
                }
                root.traversal().traverse(MetaModel.REL_NODES).withAttribute(MetaInstance.ATT_NAME, nodeName).then(foundedNode -> {
                    if (foundedNode == null || foundedNode.length == 0) {
                        node = model.createNode(0, System.currentTimeMillis());
                        node.setName(nodeName);
                        root.add(MetaModel.REL_NODES, node);
                    } else {
                        node = (Node) foundedNode[0];
                    }
                    model.save(null);
                    scheduledExecutorService.scheduleAtFixedRate(() -> {
                        OperatingSystemMXBean osBean = ManagementFactory.getOperatingSystemMXBean();
                        node.jump(System.currentTimeMillis(), currentNode -> {
                            currentNode.set(MetaNode.ATT_AVG_LOAD, osBean.getSystemLoadAverage());
                            model.save(null);
                        });
                        System.out.println("Demo print other names");
                        root.jump(System.currentTimeMillis(), currentRoot -> {
                            currentRoot.traversal().traverse(MetaModel.REL_NODES).then(nodes -> {
                                for (KObject node : nodes) {
                                    System.out.println("\t" + node.getByName("name") + ",cpu=" + node.getByName("avg_load"));
                                }
                            });
                        });

                    }, 0, 10, TimeUnit.SECONDS);
                });
            });
        });
    }

    public static void main(String[] args) {
        Random random = new Random();
        KevoreeKernel kernel = new KevoreeKernel();
        kernel.boot("ws://localhost:3080/shared", "node_" + Math.abs(random.nextInt()));
    }

}
