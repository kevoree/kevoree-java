package org.kevoree.adaptation;

import org.KevoreeModel;
import org.junit.Assert;
import org.junit.Test;
import org.kevoree.*;
import org.kevoree.modeling.memory.manager.DataManagerBuilder;
import org.kevoree.modeling.scheduler.impl.DirectScheduler;

import java.util.List;

/**
 * Created by mleduc on 14/12/15.
 */
public class AdaptationActionsTest {
    private NodeEngine nodeEngine = new NodeEngine();

    @Test
    public void testUnchanged() throws Exception {
        final KevoreeModel tm = new KevoreeModel(DataManagerBuilder.create().withScheduler(new DirectScheduler()).build());
        tm.connect(cb -> {
            final Node node1 = tm.createNode(0, 0);
            final Node node2 = tm.createNode(0, 0);
            final List<String> stringStream = nodeEngine.diff(node1, node2).toBlocking().first();
            Assert.assertNotNull(stringStream);
            Assert.assertEquals(0, stringStream.size());
        });
    }

    @Test
    public void testSubnodeAdded() throws Exception {
        final KevoreeModel tm = new KevoreeModel(DataManagerBuilder.create().withScheduler(new DirectScheduler()).build());
        tm.connect(cb -> {
            final Node node1 = tm.createNode(0, 0);
            final Node node2 = tm.createNode(0, 0);
            final Node node = tm.createNode(0, 0);
            node.setName("subNode0");
            node2.addSubNodes(node);
            final List<String> cb2 = nodeEngine.diff(node1, node2).toBlocking().first();
            Assert.assertNotNull(cb2);
            Assert.assertEquals(1, cb2.size());
            Assert.assertEquals("add subNode subNode0", cb2.get(0));
        });
    }

    @Test
    public void testSubnodeRemoved() throws Exception {
        final KevoreeModel tm = new KevoreeModel(DataManagerBuilder.create().withScheduler(new DirectScheduler()).build());
        tm.connect(cb -> {
            final Node node1 = tm.createNode(0, 0);
            final Node node = tm.createNode(0, 0);
            node.setName("subNode0");
            node1.addSubNodes(node);

            final Node node2 = tm.createNode(0, 0);
            final List<String> cb2 = nodeEngine.diff(node1, node2).first().toBlocking().first();
            Assert.assertNotNull(cb2);
            Assert.assertEquals(1, cb2.size());
            Assert.assertEquals("remove subNode subNode0", cb2.get(0));
        });
    }

    @Test
    public void testTypeDefChanged() throws Exception {
        final KevoreeModel tm = new KevoreeModel(DataManagerBuilder.create().withScheduler(new DirectScheduler()).build());
        tm.connect(cb -> {
            final Node parentNode1 = initNodeWithParent(tm, 100);
            final Node parentNode2 = initNodeWithParent(tm, 101);

            final List<String> cb2 = nodeEngine.diff(parentNode1, parentNode2).first().toBlocking().first();
            Assert.assertNotNull(cb2);
            Assert.assertEquals(2, cb2.size());
            Assert.assertEquals("remove subNode subNode0", cb2.get(0));
            Assert.assertEquals("add subNode subNode0", cb2.get(1));
        });
    }

    private Node initNodeWithParent(KevoreeModel tm, Integer tdVersion) {
        final Node parentNode = tm.createNode(0, 0);
        final Node subNode = tm.createNode(0, 0);
        subNode.setName("subNode0");
        parentNode.addSubNodes(subNode);
        final TypeDefinition tdA = tm.createNodeType(0, 0);
        tdA.setName("javaNode");
        tdA.setVersion(tdVersion);
        subNode.addTypeDefinition(tdA);

        return parentNode;
    }

    @Test
    public void testSubnodeRenamed() throws Exception {
        final KevoreeModel tm = new KevoreeModel(DataManagerBuilder.create().withScheduler(new DirectScheduler()).build());
        tm.connect(cb -> {
            final Node node1 = tm.createNode(0, 0);
            final Node nodeA = tm.createNode(0, 0);
            nodeA.setName("subNode0");
            node1.addSubNodes(nodeA);

            final Node node2 = tm.createNode(0, 0);
            final Node nodeB = tm.createNode(0, 0);
            nodeB.setName("subNode1");
            node2.addSubNodes(nodeB);
            final List<String> cb2 = nodeEngine.diff(node1, node2).toBlocking().first();
            Assert.assertNotNull(cb2);
            Assert.assertEquals(2, cb2.size());
            Assert.assertEquals("remove subNode subNode0", cb2.get(0));
            Assert.assertEquals("add subNode subNode1", cb2.get(1));
        });
    }


    @Test
    public void testComponentAdded() throws Exception {
        final KevoreeModel tm = new KevoreeModel(DataManagerBuilder.create().withScheduler(new DirectScheduler()).build());
        tm.connect(cb -> {
            final Node node1 = tm.createNode(0, 0);
            final Node node2 = tm.createNode(0, 0);
            final Component component = tm.createComponent(0, 0);
            component.setName("cmp0");
            node2.addComponents(component);
            final List<String> cb2 = nodeEngine.diff(node1, node2).toBlocking().first();
            Assert.assertNotNull(cb2);
            Assert.assertEquals(1, cb2.size());
            Assert.assertEquals("add component cmp0", cb2.get(0));
        });
    }

    @Test
    public void testComponentTypeDefChanged() throws Exception {
        final KevoreeModel tm = new KevoreeModel(DataManagerBuilder.create().withScheduler(new DirectScheduler()).build());
        tm.connect(cb -> {
            final Node node1 = tm.createNode(0, 0);
            final Component componentA = tm.createComponent(0, 0);
            componentA.setName("comp0");
            TypeDefinition tdA = tm.createComponentType(0, 0);
            tdA.setName("ticker");
            tdA.setVersion(100);
            componentA.addTypeDefinition(tdA);
            node1.addComponents(componentA);

            final Node node2 = tm.createNode(0, 0);
            final Component componentB = tm.createComponent(0, 0);
            componentB.setName("comp0");
            TypeDefinition tdB = tm.createComponentType(0, 0);
            tdB.setName("consoleprinter");
            tdB.setVersion(100);
            componentB.addTypeDefinition(tdB);
            node2.addComponents(componentB);
            final List<String> cb2 = nodeEngine.diff(node1, node2).toBlocking().first();
            Assert.assertNotNull(cb2);
            Assert.assertEquals(2, cb2.size());
            Assert.assertEquals("remove component comp0", cb2.get(0));
            Assert.assertEquals("add component comp0", cb2.get(1));
        });
    }

    @Test
    public void testComponentRemoved() throws Exception {
        final KevoreeModel tm = new KevoreeModel(DataManagerBuilder.create().withScheduler(new DirectScheduler()).build());
        tm.connect(cb -> {
            final Node node1 = tm.createNode(0, 0);
            final Component component = tm.createComponent(0, 0);
            component.setName("comp0");
            node1.addComponents(component);

            final Node node2 = tm.createNode(0, 0);
            final List<String> cb2 = nodeEngine.diff(node1, node2).toBlocking().first();
            Assert.assertNotNull(cb2);
            Assert.assertEquals(1, cb2.size());
            Assert.assertEquals("remove component comp0", cb2.get(0));
        });
    }

    @Test
    public void testComponentRenamed() throws Exception {
        final KevoreeModel tm = new KevoreeModel(DataManagerBuilder.create().withScheduler(new DirectScheduler()).build());
        tm.connect(cb -> {
            final Node node1 = tm.createNode(0, 0);
            final Component componentA = tm.createComponent(0, 0);
            componentA.setName("comp0");
            node1.addComponents(componentA);

            final Node node2 = tm.createNode(0, 0);
            final Component componentB = tm.createComponent(0, 0);
            componentB.setName("comp1");
            node2.addComponents(componentB);
            final List<String> cb2 = nodeEngine.diff(node1, node2).toBlocking().first();
            Assert.assertNotNull(cb2);
            Assert.assertEquals(2, cb2.size());
            Assert.assertEquals("remove component comp0", cb2.get(0));
            Assert.assertEquals("add component comp1", cb2.get(1));
        });
    }

    @Test
    public void testParamValueChangedRenamed() throws Exception {
        final KevoreeModel tm = new KevoreeModel(DataManagerBuilder.create().withScheduler(new DirectScheduler()).build());
        tm.connect(cb -> {
            final Node node1 = tm.createNode(0, 0);
            final Dictionary dicoA = tm.createDictionary(0, 0);
            final BooleanParam booleanParamA = tm.createBooleanParam(0, 0);
            booleanParamA.setName("param0");
            dicoA.addParams(booleanParamA);
            node1.addDictionary(dicoA);

            final Node node2 = tm.createNode(0, 0);
            final Dictionary dicB = tm.createDictionary(0, 0);
            final BooleanParam booleanParam = tm.createBooleanParam(0, 0);
            booleanParam.setName("param0");
            booleanParam.setValue(false);
            dicB.addParams(booleanParam);
            node2.addDictionary(dicB);
            final List<String> cb2 = nodeEngine.diff(node1, node2).toBlocking().first();
            Assert.assertNotNull(cb2);
            Assert.assertEquals(1, cb2.size());
            Assert.assertEquals("update param param0", cb2.get(0));
        });
    }

    @Test
    public void testGroupTypeDefChanged() throws Exception {
        final KevoreeModel tm = new KevoreeModel(DataManagerBuilder.create().withScheduler(new DirectScheduler()).build());
        tm.connect(cb -> {
            final Node node0 = tm.createNode(0, 0);
            node0.setName("node");
            final Group group0 = tm.createGroup(0, 0);
            group0.setName("group0");
            final GroupType groupType0 = tm.createGroupType(0, 0);
            groupType0.setName("remotews");
            groupType0.setVersion(100);
            group0.addTypeDefinition(groupType0);
            node0.addGroups(group0);

            final Node node1 = tm.createNode(0, 0);
            node1.setName("node");
            final Group group1 = tm.createGroup(0, 0);
            group1.setName("group0");
            final GroupType groupType1 = tm.createGroupType(0, 0);
            groupType1.setName("remotews");
            groupType1.setVersion(200);
            group1.addTypeDefinition(groupType1);
            node1.addGroups(group1);

            final List<String> cb2 = nodeEngine.diff(node0, node1).toBlocking().first();
            Assert.assertNotNull(cb2);
            Assert.assertEquals(2, cb2.size());
            Assert.assertEquals("remove group group0", cb2.get(0));
            Assert.assertEquals("add group group0", cb2.get(1));
        });
    }
}
