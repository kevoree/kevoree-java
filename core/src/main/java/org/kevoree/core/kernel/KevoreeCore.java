package org.kevoree.core.kernel;

import org.KevoreeModel;
import org.kevoree.Instance;
import org.kevoree.Model;
import org.kevoree.Node;
import org.kevoree.core.api.Command;
import org.kevoree.core.api.Core;
import org.kevoree.core.command.AddInstance;
import org.kevoree.meta.MetaInstance;
import org.kevoree.meta.MetaModel;
import org.kevoree.modeling.KListener;
import org.kevoree.modeling.KObject;
import org.kevoree.modeling.memory.manager.DataManagerBuilder;

import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;

public class KevoreeCore implements Core, Runnable {

    private Map<String, Command> commands = new HashMap<String, Command>();
    private KevoreeModel model;
    private Model root;
    private Node node;
    private ScheduledExecutorService mono_scheduler = Executors.newSingleThreadScheduledExecutor();
    public static final String ADD_INSTANCE = "add_instance";
    public static final String REMOVE_INSTANCE = "remove_instance";

    public KevoreeCore() {
        commands.put(ADD_INSTANCE, new AddInstance());
        commands.put(REMOVE_INSTANCE, new AddInstance());
    }

    public void boot(String url, String nodeName) {
        model = new KevoreeModel(DataManagerBuilder.create().withContentDeliveryDriver(new org.kevoree.modeling.drivers.websocket.WebSocketPeer(url)).build());
        model.connect(o -> {
            KListener listener = model.createListener(0);
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
                    listener.listen(node);
                    listener.then(o1 -> mono_scheduler.schedule(this, 0, TimeUnit.SECONDS));
                    mono_scheduler.scheduleAtFixedRate(this, 0, 5, TimeUnit.SECONDS);
                });
            });
        });
    }

    @Override
    public Model model() {
        return root;
    }

    @Override
    public Node node() {
        return node;
    }

    @Override
    public Object instance(String name) {
        return null;
    }

    @Override
    public String[] instanceNames() {
        return new String[0];
    }

    @Override
    public void remove(String name) {

    }

    @Override
    public void add(String name) {

    }

    @Override
    public void run() {
        //Here we check the consistency of the model according to
        Node pastNode = node();
        if (pastNode != null) {
            pastNode.jump(System.currentTimeMillis(), (KObject currentNodeUncasted) -> {
                Node currentNode = (Node) currentNodeUncasted;
                currentNode.getInstances(instances -> {
                    Map<String, Instance> nextIntances = new HashMap<String, Instance>(instances.length);
                    String[] previousInstances = instanceNames();
                    for (Instance instance : instances) {
                        nextIntances.put(instance.getName(), instance);
                        Object existingInstance = instance(instance.getName());
                        if (existingInstance == null) {
                            Command cmd = commands.get(ADD_INSTANCE);
                            if (cmd != null) {
                                cmd.run(this, instance);
                            }
                        }
                    }
                    for (String previousName : previousInstances) {
                        if (!nextIntances.containsKey(previousName)) {
                            Command cmd = commands.get(REMOVE_INSTANCE);
                            if (cmd != null) {
                                cmd.run(this, instance(previousName));
                            }
                        }
                    }
                });
            });
        }
    }
}
