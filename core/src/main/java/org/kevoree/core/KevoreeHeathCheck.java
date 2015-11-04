package org.kevoree.core;

public class KevoreeHeathCheck implements Runnable {

    @Override
    public void run() {
        //TODO INJECT the HEATH CHECK
                    /*
                    scheduledExecutorService.scheduleAtFixedRate(() -> {
                        OperatingSystemMXBean osBean = ManagementFactory.getOperatingSystemMXBean();
                        node.jump(System.currentTimeMillis(), currentNode -> {
                            //currentNode.set(MetaNode.ATT_AVG_LOAD, osBean.getSystemLoadAverage());
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
                    */
    }
}
