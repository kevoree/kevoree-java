package org.kevoree.adaptation;

import org.KevoreeModel;
import org.junit.Assert;
import org.junit.Test;
import org.kevoree.*;
import org.kevoree.adaptation.operation.UpdateInstance;
import org.kevoree.adaptation.operation.util.AdaptationOperation;
import org.kevoree.adaptation.operation.AddInstance;
import org.kevoree.adaptation.operation.RemoveInstance;
import org.kevoree.modeling.KCallback;
import org.kevoree.modeling.memory.manager.DataManagerBuilder;
import org.kevoree.modeling.scheduler.impl.DirectScheduler;

import java.util.SortedSet;
import java.util.TreeSet;

import static org.assertj.core.api.Assertions.assertThat;

/**
 * Created by mleduc on 14/12/15.
 */
public class NodeEngineTest {
    private NodeEngine nodeEngine = new NodeEngine();

    /**
     * Prev : Empty node
     * Next : Empty node
     * Result : no operation
     *
     * @throws Exception
     */
    @Test
    public void testUnchanged() throws Exception {
        final KevoreeModel tm = new KevoreeModel(DataManagerBuilder.create().withScheduler(new DirectScheduler()).build());
        tm.connect(new KCallback() {
            @Override
            public void on(Object cb) {
                final Node node1 = tm.createNode(0, 0);
                final Node node2 = tm.createNode(0, 0);
                final SortedSet<AdaptationOperation> stringStream = nodeEngine.diff(node1, node2).toBlocking().first();
                Assert.assertNotNull(stringStream);
                Assert.assertEquals(0, stringStream.size());
            }
        });
    }

    /**
     * Prev : Node with no subnode
     * Next : A subnode is added
     * Result : A add instance operation is asked for the new subnode.
     *
     * @throws Exception
     */
    @Test
    public void testSubnodeAdded() throws Exception {
        final KevoreeModel tm = new KevoreeModel(DataManagerBuilder.create().withScheduler(new DirectScheduler()).build());
        tm.connect(new KCallback() {
            @Override
            public void on(Object cb) {
                final Node node1 = tm.createNode(0, 0);
                final Node node2 = tm.createNode(0, 0);
                final Node node = tm.createNode(0, 0);
                node.setName("subNode0");
                node2.addSubNodes(node);
                final SortedSet<AdaptationOperation> cb2 = nodeEngine.diff(node1, node2).toBlocking().first();
                Assert.assertNotNull(cb2);
                Assert.assertEquals(1, cb2.size());
                final TreeSet<AdaptationOperation> expected = new TreeSet<>();
                expected.add(new AddInstance(node.uuid()));
                assertThat(cb2).containsExactlyElementsOf(expected);
            }
        });
    }

    /**
     *
     * @throws Exception
     */
    @Test
    public void testSubnodeRemoved() throws Exception {
        final KevoreeModel tm = new KevoreeModel(DataManagerBuilder.create().withScheduler(new DirectScheduler()).build());
        tm.connect(new KCallback() {
            @Override
            public void on(Object cb) {
                final Node node1 = tm.createNode(0, 0);
                final Node node = tm.createNode(0, 0);
                node.setName("subNode0");
                node1.addSubNodes(node);

                final Node node2 = tm.createNode(0, 0);
                final SortedSet<AdaptationOperation> cb2 = nodeEngine.diff(node1, node2).first().toBlocking().first();
                Assert.assertNotNull(cb2);
                Assert.assertEquals(1, cb2.size());
                final TreeSet<AdaptationOperation> expected = new TreeSet<>();
                expected.add(new RemoveInstance(node.uuid()));
                assertThat(cb2).containsExactlyElementsOf(expected);
            }
        });
    }

    @Test
    public void testTypeDefChanged() throws Exception {
        final KevoreeModel tm = new KevoreeModel(DataManagerBuilder.create().withScheduler(new DirectScheduler()).build());
        tm.connect(new KCallback() {
            @Override
            public void on(Object cb) {
                final Node parentNode1 = tm.createNode(0, 0);
                final Node subNode1 = tm.createNode(0, 0);
                subNode1.setName("subNode0");
                parentNode1.addSubNodes(subNode1);
                final TypeDefinition tdA = tm.createNodeType(0, 0);
                tdA.setName("javaNode");
                tdA.setVersion(100);
                subNode1.addTypeDefinition(tdA);

                final Node parentNode2 = tm.createNode(0, 0);
                final Node subNode2 = tm.createNode(0, 0);
                subNode2.setName("subNode0");
                parentNode2.addSubNodes(subNode2);
                final TypeDefinition tdB = tm.createNodeType(0, 0);
                tdB.setName("javaNode");
                tdB.setVersion(101);
                subNode2.addTypeDefinition(tdB);

                final SortedSet<AdaptationOperation> cb2 = nodeEngine.diff(parentNode1, parentNode2).first().toBlocking().first();
                Assert.assertNotNull(cb2);
                Assert.assertEquals(2, cb2.size());
                final TreeSet<AdaptationOperation> expected = new TreeSet<>();
                expected.add(new RemoveInstance(subNode1.uuid()));
                expected.add(new AddInstance(subNode2.uuid()));
                assertThat(cb2).containsExactlyElementsOf(expected);
            }
        });
    }

    @Test
    public void testSubnodeRenamed() throws Exception {
        final KevoreeModel tm = new KevoreeModel(DataManagerBuilder.create().withScheduler(new DirectScheduler()).build());
        tm.connect(new KCallback() {
            @Override
            public void on(Object cb) {
                final Node node1 = tm.createNode(0, 0);
                final Node nodeA = tm.createNode(0, 0);
                nodeA.setName("subNode0");
                node1.addSubNodes(nodeA);

                final Node node2 = tm.createNode(0, 0);
                final Node nodeB = tm.createNode(0, 0);
                nodeB.setName("subNode1");
                node2.addSubNodes(nodeB);
                final SortedSet<AdaptationOperation> cb2 = nodeEngine.diff(node1, node2).toBlocking().first();
                Assert.assertNotNull(cb2);
                Assert.assertEquals(2, cb2.size());
                final TreeSet<AdaptationOperation> expected = new TreeSet<>();
                expected.add(new RemoveInstance(nodeA.uuid()));
                expected.add(new AddInstance(nodeB.uuid()));
                assertThat(cb2).containsExactlyElementsOf(expected);
            }
        });
    }


    @Test
    public void testComponentAdded() throws Exception {
        final KevoreeModel tm = new KevoreeModel(DataManagerBuilder.create().withScheduler(new DirectScheduler()).build());
        tm.connect(new KCallback() {
            @Override
            public void on(Object cb) {
                final Node node1 = tm.createNode(0, 0);
                final Node node2 = tm.createNode(0, 0);
                final Component component = tm.createComponent(0, 0);
                component.setName("cmp0");
                node2.addComponents(component);
                final SortedSet<AdaptationOperation> cb2 = nodeEngine.diff(node1, node2).toBlocking().first();
                Assert.assertNotNull(cb2);
                Assert.assertEquals(1, cb2.size());
                final TreeSet<AdaptationOperation> expected = new TreeSet<>();
                expected.add(new AddInstance(component.uuid()));
                assertThat(cb2).containsExactlyElementsOf(expected);
            }
        });
    }

    @Test
    public void testComponentTypeDefChanged() throws Exception {
        final KevoreeModel tm = new KevoreeModel(DataManagerBuilder.create().withScheduler(new DirectScheduler()).build());
        tm.connect(new KCallback() {
            @Override
            public void on(Object cb) {
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
                final SortedSet<AdaptationOperation> cb2 = nodeEngine.diff(node1, node2).toBlocking().first();
                Assert.assertNotNull(cb2);
                Assert.assertEquals(2, cb2.size());
                final TreeSet<AdaptationOperation> expected = new TreeSet<>();
                expected.add(new AddInstance(componentB.uuid()));
                expected.add(new RemoveInstance(componentA.uuid()));
                assertThat(cb2).containsExactlyElementsOf(expected);
            }
        });
    }

    @Test
    public void testComponentRemoved() throws Exception {
        final KevoreeModel tm = new KevoreeModel(DataManagerBuilder.create().withScheduler(new DirectScheduler()).build());
        tm.connect(new KCallback() {
            @Override
            public void on(Object cb) {
                final Node node1 = tm.createNode(0, 0);
                final Component component = tm.createComponent(0, 0);
                component.setName("comp0");
                node1.addComponents(component);

                final Node node2 = tm.createNode(0, 0);
                final SortedSet<AdaptationOperation> cb2 = nodeEngine.diff(node1, node2).toBlocking().first();
                Assert.assertNotNull(cb2);
                Assert.assertEquals(1, cb2.size());
                final TreeSet<AdaptationOperation> expected = new TreeSet<>();
                expected.add(new RemoveInstance(component.uuid()));
                assertThat(cb2).containsExactlyElementsOf(expected);
            }
        });
    }

    @Test
    public void testComponentRenamed() throws Exception {
        final KevoreeModel tm = new KevoreeModel(DataManagerBuilder.create().withScheduler(new DirectScheduler()).build());
        tm.connect(new KCallback() {
            @Override
            public void on(Object cb) {
                final Node node1 = tm.createNode(0, 0);
                final Component componentA = tm.createComponent(0, 0);
                componentA.setName("comp0");
                node1.addComponents(componentA);

                final Node node2 = tm.createNode(0, 0);
                final Component componentB = tm.createComponent(0, 0);
                componentB.setName("comp1");
                node2.addComponents(componentB);
                final SortedSet<AdaptationOperation> cb2 = nodeEngine.diff(node1, node2).toBlocking().first();
                Assert.assertNotNull(cb2);
                Assert.assertEquals(2, cb2.size());
                final TreeSet<AdaptationOperation> expected = new TreeSet<>();
                expected.add(new RemoveInstance(componentA.uuid()));
                expected.add(new AddInstance(componentB.uuid()));
                assertThat(cb2).containsExactlyElementsOf(expected);
            }
        });
    }

    @Test
    public void testBooleanParamValueChangedRenamed() throws Exception {
        final KevoreeModel tm = new KevoreeModel(DataManagerBuilder.create().withScheduler(new DirectScheduler()).build());
        tm.connect(new KCallback() {
            @Override
            public void on(Object cb) {
                final Node node1 = tm.createNode(0, 0);
                final Dictionary dicoA = tm.createDictionary(0, 0);
                final BooleanParam booleanParamA = tm.createBooleanParam(0, 0);
                booleanParamA.setName("param0");
                dicoA.addParams(booleanParamA);
                node1.addDictionary(dicoA);

                final Node node2 = tm.createNode(0, 0);
                final Dictionary dicB = tm.createDictionary(0, 0);
                final BooleanParam booleanParamB = tm.createBooleanParam(0, 0);
                booleanParamB.setName("param0");
                booleanParamB.setValue(false);
                dicB.addParams(booleanParamB);
                node2.addDictionary(dicB);
                final SortedSet<AdaptationOperation> cb2 = nodeEngine.diff(node1, node2).toBlocking().first();
                Assert.assertNotNull(cb2);
                Assert.assertEquals(1, cb2.size());
                final TreeSet<AdaptationOperation> expected = new TreeSet<>();
                expected.add(new UpdateInstance(booleanParamB.uuid()));
                assertThat(cb2).containsExactlyElementsOf(expected);
            }
        });
    }

    @Test
    public void testListParamValueAdded() throws Exception {
        final KevoreeModel tm = new KevoreeModel(DataManagerBuilder.create().withScheduler(new DirectScheduler()).build());
        tm.connect(new KCallback() {
            @Override
            public void on(Object cb) {
                final Node node1 = tm.createNode(0, 0);
                final Dictionary dicoA = tm.createDictionary(0, 0);
                final ListParam listParamA = tm.createListParam(0, 0);
                listParamA.setName("param0");
                dicoA.addParams(listParamA);
                node1.addDictionary(dicoA);

                final Node node2 = tm.createNode(0, 0);
                final Dictionary dicB = tm.createDictionary(0, 0);
                final ListParam listParamB = tm.createListParam(0, 0);
                listParamB.setName("param0");
                final Item item1 = tm.createItem(0, 0);
                item1.setValue("1");
                listParamB.addValues(item1);
                dicB.addParams(listParamB);
                node2.addDictionary(dicB);
                final SortedSet<AdaptationOperation> cb2 = nodeEngine.diff(node1, node2).toBlocking().first();
                Assert.assertNotNull(cb2);
                Assert.assertEquals(1, cb2.size());
                final TreeSet<AdaptationOperation> expected = new TreeSet<>();
                expected.add(new UpdateInstance(listParamB.uuid()));
                assertThat(cb2).containsExactlyElementsOf(expected);
            }
        });
    }

    @Test
    public void testListParamValueChanged() throws Exception {
        final KevoreeModel tm = new KevoreeModel(DataManagerBuilder.create().withScheduler(new DirectScheduler()).build());
        tm.connect(new KCallback() {
            @Override
            public void on(Object cb) {
                final Node node1 = tm.createNode(0, 0);
                final Dictionary dicoA = tm.createDictionary(0, 0);
                final ListParam listParamA = tm.createListParam(0, 0);
                listParamA.setName("param0");
                Item item01 = tm.createItem(0, 0);
                item01.setValue("0");
                listParamA.addValues(item01);
                dicoA.addParams(listParamA);
                node1.addDictionary(dicoA);

                final Node node2 = tm.createNode(0, 0);
                final Dictionary dicB = tm.createDictionary(0, 0);
                final ListParam listParamB = tm.createListParam(0, 0);
                listParamB.setName("param0");
                final Item item1 = tm.createItem(0, 0);
                item1.setValue("1");
                listParamB.addValues(item1);
                dicB.addParams(listParamB);
                node2.addDictionary(dicB);
                final SortedSet<AdaptationOperation> cb2 = nodeEngine.diff(node1, node2).toBlocking().first();
                Assert.assertNotNull(cb2);
                Assert.assertEquals(1, cb2.size());
                final TreeSet<AdaptationOperation> expected = new TreeSet<>();
                expected.add(new UpdateInstance(listParamB.uuid()));
                assertThat(cb2).containsExactlyElementsOf(expected);
            }
        });
    }

    @Test
    public void testListParamValueUnhanged() throws Exception {
        final KevoreeModel tm = new KevoreeModel(DataManagerBuilder.create().withScheduler(new DirectScheduler()).build());
        tm.connect(new KCallback() {
            @Override
            public void on(Object cb) {
                final Node node1 = tm.createNode(0, 0);
                final Dictionary dicoA = tm.createDictionary(0, 0);
                final ListParam listParamA = tm.createListParam(0, 0);
                listParamA.setName("param0");
                Item item01 = tm.createItem(0, 0);
                item01.setValue("0");
                listParamA.addValues(item01);
                dicoA.addParams(listParamA);
                node1.addDictionary(dicoA);

                final Node node2 = tm.createNode(0, 0);
                final Dictionary dicB = tm.createDictionary(0, 0);
                final ListParam listParamB = tm.createListParam(0, 0);
                listParamB.setName("param0");
                final Item item1 = tm.createItem(0, 0);
                item1.setValue("0");
                listParamB.addValues(item1);
                dicB.addParams(listParamB);
                node2.addDictionary(dicB);
                final SortedSet<AdaptationOperation> cb2 = nodeEngine.diff(node1, node2).toBlocking().first();
                Assert.assertNotNull(cb2);
                Assert.assertEquals(0, cb2.size());
            }
        });
    }

    @Test
    public void testGroupTypeDefChanged() throws Exception {
        final KevoreeModel tm = new KevoreeModel(DataManagerBuilder.create().withScheduler(new DirectScheduler()).build());
        tm.connect(new KCallback() {
            @Override
            public void on(Object cb) {
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

                final SortedSet<AdaptationOperation> cb2 = nodeEngine.diff(node0, node1).toBlocking().first();
                Assert.assertNotNull(cb2);
                Assert.assertEquals(2, cb2.size());
                final TreeSet<AdaptationOperation> expected = new TreeSet<>();
                expected.add(new RemoveInstance(group0.uuid()));
                expected.add(new AddInstance(group1.uuid()));
                assertThat(cb2).containsExactlyElementsOf(expected);
            }
        });
    }
}
