package org.kevoree.adaptation;

import org.KevoreeModel;
import org.junit.Assert;
import org.junit.Test;
import org.kevoree.*;
import org.kevoree.adaptation.operation.*;
import org.kevoree.adaptation.operation.util.AdaptationOperation;
import org.kevoree.modeling.KCallback;
import org.kevoree.modeling.memory.manager.DataManagerBuilder;
import org.kevoree.modeling.scheduler.impl.DirectScheduler;

import java.util.SortedSet;
import java.util.TreeSet;

import static org.assertj.core.api.Assertions.assertThat;

/**
 * TODO :
 * - what happen if a new type def applicable to the platform is added to a deploy unit at runtime. Should remove the current instance and start another one with the new deploy unit ?
 * <p>
 * Created by mleduc on 14/12/15.
 */
public class NodeEngineTest {
    private final NodeEngine nodeEngine = new NodeEngine();
    private final String platform = "test";

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
                final SortedSet<AdaptationOperation> stringStream = nodeEngine.diff(node1, node2, platform).toBlocking().first();
                Assert.assertNotNull(stringStream);
                Assert.assertEquals(0, stringStream.size());
            }
        });
    }

    @Test
    public void testStart() throws Exception {
        final KevoreeModel tm = new KevoreeModel(DataManagerBuilder.create().withScheduler(new DirectScheduler()).build());
        tm.connect(new KCallback() {
            @Override
            public void on(Object cb) {
                final Node node0 = tm.createNode(0, 0);
                node0.setStarted(false);
                final Node node1 = tm.createNode(0, 0);
                node1.setStarted(true);
                final SortedSet<AdaptationOperation> stringStream = nodeEngine.diff(node0, node1, platform).toBlocking().first();
                Assert.assertNotNull(stringStream);
                final TreeSet<AdaptationOperation> expected = new TreeSet<>();
                expected.add(new StartInstance(node1));
                assertThat(stringStream).containsExactlyElementsOf(expected);
            }
        });
    }

    @Test
    public void testNotStartWhenInitialStatusNotDefined() throws Exception {
        final KevoreeModel tm = new KevoreeModel(DataManagerBuilder.create().withScheduler(new DirectScheduler()).build());
        tm.connect(new KCallback() {
            @Override
            public void on(Object cb) {
                final Node node0 = tm.createNode(0, 0);
                final Node node1 = tm.createNode(0, 0);
                node1.setStarted(false);
                final SortedSet<AdaptationOperation> stringStream = nodeEngine.diff(node0, node1, platform).toBlocking().first();
                Assert.assertNotNull(stringStream);
                final TreeSet<AdaptationOperation> expected = new TreeSet<>();
                assertThat(stringStream).containsExactlyElementsOf(expected);
            }
        });
    }


    @Test
    public void testStartWhenInitialStatusNotDefined() throws Exception {
        final KevoreeModel tm = new KevoreeModel(DataManagerBuilder.create().withScheduler(new DirectScheduler()).build());
        tm.connect(new KCallback() {
            @Override
            public void on(Object cb) {
                final Node node0 = tm.createNode(0, 0);
                final Node node1 = tm.createNode(0, 0);
                node1.setStarted(true);
                final SortedSet<AdaptationOperation> stringStream = nodeEngine.diff(node0, node1, platform).toBlocking().first();
                Assert.assertNotNull(stringStream);
                final TreeSet<AdaptationOperation> expected = new TreeSet<>();
                expected.add(new StartInstance(node1));
                assertThat(stringStream).containsExactlyElementsOf(expected);
            }
        });
    }

    @Test
    public void testStop() throws Exception {
        final KevoreeModel tm = new KevoreeModel(DataManagerBuilder.create().withScheduler(new DirectScheduler()).build());
        tm.connect(new KCallback() {
            @Override
            public void on(Object cb) {
                final Node node0 = tm.createNode(0, 0);
                node0.setStarted(true);
                final Node node1 = tm.createNode(0, 0);
                node1.setStarted(false);
                final SortedSet<AdaptationOperation> stringStream = nodeEngine.diff(node0, node1, platform).toBlocking().first();
                Assert.assertNotNull(stringStream);
                final TreeSet<AdaptationOperation> expected = new TreeSet<>();
                expected.add(new StopInstance(node1));
                assertThat(stringStream).containsExactlyElementsOf(expected);
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
                final SortedSet<AdaptationOperation> cb2 = nodeEngine.diff(node1, node2, platform).toBlocking().first();
                Assert.assertNotNull(cb2);
                Assert.assertEquals(1, cb2.size());
                final TreeSet<AdaptationOperation> expected = new TreeSet<>();
                expected.add(new AddInstance(node));
                assertThat(cb2).containsExactlyElementsOf(expected);
            }
        });
    }

    /**
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
                final SortedSet<AdaptationOperation> cb2 = nodeEngine.diff(node1, node2, platform).first().toBlocking().first();
                Assert.assertNotNull(cb2);
                Assert.assertEquals(1, cb2.size());
                final TreeSet<AdaptationOperation> expected = new TreeSet<>();
                expected.add(new RemoveInstance(node));
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

                final SortedSet<AdaptationOperation> cb2 = nodeEngine.diff(parentNode1, parentNode2, platform).first().toBlocking().first();
                Assert.assertNotNull(cb2);
                Assert.assertEquals(2, cb2.size());
                final TreeSet<AdaptationOperation> expected = new TreeSet<>();
                expected.add(new RemoveInstance(subNode1));
                expected.add(new AddInstance(subNode2));
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
                final SortedSet<AdaptationOperation> cb2 = nodeEngine.diff(node1, node2, platform).toBlocking().first();
                Assert.assertNotNull(cb2);
                Assert.assertEquals(2, cb2.size());
                final TreeSet<AdaptationOperation> expected = new TreeSet<>();
                expected.add(new RemoveInstance(nodeA));
                expected.add(new AddInstance(nodeB));
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
                final SortedSet<AdaptationOperation> cb2 = nodeEngine.diff(node1, node2, platform).toBlocking().first();
                Assert.assertNotNull(cb2);
                Assert.assertEquals(1, cb2.size());
                final TreeSet<AdaptationOperation> expected = new TreeSet<>();
                expected.add(new AddInstance(component));
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
                final SortedSet<AdaptationOperation> cb2 = nodeEngine.diff(node1, node2, platform).toBlocking().first();
                Assert.assertNotNull(cb2);
                Assert.assertEquals(2, cb2.size());
                final TreeSet<AdaptationOperation> expected = new TreeSet<>();
                expected.add(new AddInstance(componentB));
                expected.add(new RemoveInstance(componentA));
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
                final SortedSet<AdaptationOperation> cb2 = nodeEngine.diff(node1, node2, platform).toBlocking().first();
                Assert.assertNotNull(cb2);
                Assert.assertEquals(1, cb2.size());
                final TreeSet<AdaptationOperation> expected = new TreeSet<>();
                expected.add(new RemoveInstance(component));
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
                final SortedSet<AdaptationOperation> cb2 = nodeEngine.diff(node1, node2, platform).toBlocking().first();
                Assert.assertNotNull(cb2);
                Assert.assertEquals(2, cb2.size());
                final TreeSet<AdaptationOperation> expected = new TreeSet<>();
                expected.add(new RemoveInstance(componentA));
                expected.add(new AddInstance(componentB));
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
                final SortedSet<AdaptationOperation> cb2 = nodeEngine.diff(node1, node2, platform).toBlocking().first();
                Assert.assertNotNull(cb2);
                Assert.assertEquals(2, cb2.size());
                final TreeSet<AdaptationOperation> expected = new TreeSet<>();
                expected.add(new UpdateParam(booleanParamB));
                expected.add(new UpdateInstance(node2));
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
                final SortedSet<AdaptationOperation> cb2 = nodeEngine.diff(node1, node2, platform).toBlocking().first();
                Assert.assertNotNull(cb2);
                Assert.assertEquals(2, cb2.size());
                final TreeSet<AdaptationOperation> expected = new TreeSet<>();
                expected.add(new UpdateParam(listParamB));
                expected.add(new UpdateInstance(node2));
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
                final SortedSet<AdaptationOperation> cb2 = nodeEngine.diff(node1, node2, platform).toBlocking().first();
                Assert.assertNotNull(cb2);
                final TreeSet<AdaptationOperation> expected = new TreeSet<>();
                expected.add(new UpdateParam(listParamB));
                expected.add(new UpdateInstance(node2));
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
                final SortedSet<AdaptationOperation> cb2 = nodeEngine.diff(node1, node2, platform).toBlocking().first();
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

                final SortedSet<AdaptationOperation> cb2 = nodeEngine.diff(node0, node1, platform).toBlocking().first();
                Assert.assertNotNull(cb2);
                Assert.assertEquals(2, cb2.size());
                final TreeSet<AdaptationOperation> expected = new TreeSet<>();
                expected.add(new RemoveInstance(group0));
                expected.add(new AddInstance(group1));
                assertThat(cb2).containsExactlyElementsOf(expected);
            }
        });
    }

    /**
     * We link an existing component to a chan. The channel is instanciated.
     */
    @Test
    public void testAddChannel() {
        final KevoreeModel tm = new KevoreeModel(DataManagerBuilder.create().withScheduler(new DirectScheduler()).build());

        tm.connect(new KCallback() {
            @Override
            public void on(Object cb) {
                final Node node0 = tm.createNode(0, 0);
                final Component component0 = tm.createComponent(0, 0);
                component0.setName("comp0");
                node0.addComponents(component0);

                final Node node1 = tm.createNode(0, 0);
                final Component component1 = tm.createComponent(0, 0);
                final OutputPort outputPort1 = tm.createOutputPort(0, 0);
                final Channel channel1 = tm.createChannel(0, 0);
                component1.setName("comp0");
                channel1.setName("chan0");
                outputPort1.addChannels(channel1);
                component1.addOutputs(outputPort1);
                node1.addComponents(component1);


                final SortedSet<AdaptationOperation> cb2 = nodeEngine.diff(node0, node1, platform).toBlocking().first();
                Assert.assertNotNull(cb2);
                final TreeSet<AdaptationOperation> expected = new TreeSet<>();
                expected.add(new AddInstance(channel1));
                assertThat(cb2).containsExactlyElementsOf(expected);
            }
        });
    }

    /**
     * We add a new component and link it to an existing channel.
     * Only the component is instanciated.
     */
    @Test
    public void testAddChannelAlreadyThere() {

        final KevoreeModel tm = new KevoreeModel(DataManagerBuilder.create().withScheduler(new DirectScheduler()).build());


        tm.connect(new KCallback() {
            @Override
            public void on(Object cb) {

                final Node node0 = tm.createNode(0, 0);
                final Component component1 = tm.createComponent(0, 0);
                final OutputPort outputPort = tm.createOutputPort(0, 0);
                final Channel channel = tm.createChannel(0, 0);
                component1.setName("comp0");
                channel.setName("chan0");
                outputPort.addChannels(channel);
                component1.addOutputs(outputPort);
                node0.addComponents(component1);

                final Node node1 = tm.createNode(0, 0);
                final Component component2 = tm.createComponent(0, 0);
                final Component component3 = tm.createComponent(0, 0);
                final OutputPort outputPort2 = tm.createOutputPort(0, 0);
                final Channel channel2 = tm.createChannel(0, 0);
                component2.setName("comp0");
                component3.setName("comp1");
                channel2.setName("chan0");
                outputPort2.addChannels(channel2);
                component2.addOutputs(outputPort2);
                component3.addOutputs(outputPort2);
                node1.addComponents(component2);
                node1.addComponents(component3);


                final SortedSet<AdaptationOperation> cb2 = nodeEngine.diff(node0, node1, platform).toBlocking().first();
                Assert.assertNotNull(cb2);
                final TreeSet<AdaptationOperation> expected = new TreeSet<>();
                expected.add(new AddInstance(component3));
                assertThat(cb2).containsExactlyElementsOf(expected);
            }
        });
    }

    /**
     * We remove the relation between a component and a channel.
     * The channel is still connected to the node by another component so no adaptation is needed.
     */
    @Test
    public void testUnlinkComponentToChannelButStillLinkOnotherWay() {
        final KevoreeModel tm = new KevoreeModel(DataManagerBuilder.create().withScheduler(new DirectScheduler()).build());


        tm.connect(new KCallback() {
            @Override
            public void on(Object cb) {
                final Node node0 = tm.createNode(0, 0);
                final Component component2 = tm.createComponent(0, 0);
                final Component component3 = tm.createComponent(0, 0);
                final OutputPort outputPort2 = tm.createOutputPort(0, 0);
                final Channel channel2 = tm.createChannel(0, 0);
                component2.setName("comp0");
                component3.setName("comp1");
                channel2.setName("chan0");
                outputPort2.addChannels(channel2);
                component2.addOutputs(outputPort2);
                component3.addOutputs(outputPort2);
                node0.addComponents(component2);
                node0.addComponents(component3);

                final Node node1 = tm.createNode(0, 0);
                final Component component20 = tm.createComponent(0, 0);
                final Component component30 = tm.createComponent(0, 0);
                final OutputPort outputPort20 = tm.createOutputPort(0, 0);
                final Channel channel20 = tm.createChannel(0, 0);
                component20.setName("comp0");
                component30.setName("comp1");
                channel20.setName("chan0");
                outputPort20.addChannels(channel20);
                node1.addComponents(component20);
                node1.addComponents(component30);

                final SortedSet<AdaptationOperation> cb2 = nodeEngine.diff(node0, node1, platform).toBlocking().first();
                Assert.assertNotNull(cb2);
                final TreeSet<AdaptationOperation> expected = new TreeSet<>();
                expected.add(new RemoveInstance(channel2));
                assertThat(cb2).containsExactlyElementsOf(expected);
            }
        });
    }

    /**
     * We remove the relation between a component and a channel.
     * The channel is still connected to the node by another component so no adaptation is needed.
     */
    @Test
    public void testUnlinkComponentToChannelAndNoLinkRemain() {
        final KevoreeModel tm = new KevoreeModel(DataManagerBuilder.create().withScheduler(new DirectScheduler()).build());
        tm.connect(new KCallback() {
            @Override
            public void on(Object cb) {


                final Node node0 = tm.createNode(0, 0);
                final Component component2 = tm.createComponent(0, 0);
                final Component component3 = tm.createComponent(0, 0);
                final OutputPort outputPort2 = tm.createOutputPort(0, 0);
                final Channel channel2 = tm.createChannel(0, 0);
                component2.setName("comp0");
                component3.setName("comp1");
                channel2.setName("chan0");
                outputPort2.addChannels(channel2);
                component2.addOutputs(outputPort2);
                component3.addOutputs(outputPort2);
                node0.addComponents(component2);
                node0.addComponents(component3);

                final Node node1 = tm.createNode(0, 0);
                final Component component20 = tm.createComponent(0, 0);
                final Component component30 = tm.createComponent(0, 0);
                final OutputPort outputPort20 = tm.createOutputPort(0, 0);
                final Channel channel20 = tm.createChannel(0, 0);
                component20.setName("comp0");
                component30.setName("comp1");
                channel20.setName("chan0");
                outputPort20.addChannels(channel20);
                component20.addOutputs(outputPort20);
                node1.addComponents(component20);
                node1.addComponents(component30);


                final SortedSet<AdaptationOperation> cb2 = nodeEngine.diff(node0, node1, platform).toBlocking().first();
                Assert.assertNotNull(cb2);
                final TreeSet<AdaptationOperation> expected = new TreeSet<>();
                assertThat(cb2).containsExactlyElementsOf(expected);
            }
        });
    }

    @Test
    public void testAddDeployUnitSimple() {
        final KevoreeModel tm = new KevoreeModel(DataManagerBuilder.create().withScheduler(new DirectScheduler()).build());
        tm.connect(new KCallback() {
            @Override
            public void on(Object cb) {

                final Node node0 = tm.createNode(0, 0);

                final Node node1 = tm.createNode(0, 0);
                final Component component = tm.createComponent(0, 0);
                final ComponentType componentType = tm.createComponentType(0, 0);
                final DeployUnit deployUnit = tm.createDeployUnit(0, 0);
                deployUnit.setName("du1");
                deployUnit.setPlatform("test");
                deployUnit.setVersion("1.0.0");
                componentType.addDeployUnits(deployUnit);
                component.addTypeDefinition(componentType);
                node1.addComponents(component);

                final SortedSet<AdaptationOperation> cb2 = nodeEngine.diff(node0, node1, platform).toBlocking().first();
                Assert.assertNotNull(cb2);
                final TreeSet<AdaptationOperation> expected = new TreeSet<>();
                expected.add(new AddInstance(component));
                expected.add(new AddDeployUnit(deployUnit));
                assertThat(cb2).containsExactlyElementsOf(expected);
            }
        });
    }

    @Test
    public void testDeployUnitInWrongPlatform() {
        final KevoreeModel tm = new KevoreeModel(DataManagerBuilder.create().withScheduler(new DirectScheduler()).build());
        tm.connect(new KCallback() {
            @Override
            public void on(Object cb) {

                final Node node0 = tm.createNode(0, 0);

                final Node node1 = tm.createNode(0, 0);
                final Component component = tm.createComponent(0, 0);
                final ComponentType componentType = tm.createComponentType(0, 0);
                final DeployUnit deployUnit = tm.createDeployUnit(0, 0);
                deployUnit.setName("du1");
                deployUnit.setPlatform("doesnotexists");
                deployUnit.setVersion("1.0.0");
                componentType.addDeployUnits(deployUnit);
                component.addTypeDefinition(componentType);
                node1.addComponents(component);

                final SortedSet<AdaptationOperation> cb2 = nodeEngine.diff(node0, node1, platform).toBlocking().first();
                Assert.assertNotNull(cb2);
                final TreeSet<AdaptationOperation> expected = new TreeSet<>();
                expected.add(new AddInstance(component));
                assertThat(cb2).containsExactlyElementsOf(expected);
            }
        });
    }

    @Test
    public void testRemoveDeployUnitInWrongPlatform() {
        final KevoreeModel tm = new KevoreeModel(DataManagerBuilder.create().withScheduler(new DirectScheduler()).build());
        tm.connect(new KCallback() {
            @Override
            public void on(Object cb) {


                final Node node0 = tm.createNode(0, 0);
                final Component component0 = tm.createComponent(0, 0);
                final ComponentType typeDefinition0 = tm.createComponentType(0, 0);
                final DeployUnit deployUnit0 = tm.createDeployUnit(0, 0);
                component0.setName("comp0");
                typeDefinition0.setName("td0");
                typeDefinition0.setVersion(1);
                deployUnit0.setName("du1");
                deployUnit0.setPlatform("doesnotexists");
                deployUnit0.setVersion("1.0.0");
                typeDefinition0.addDeployUnits(deployUnit0);
                component0.addTypeDefinition(typeDefinition0);
                node0.addComponents(component0);


                final Node node1 = tm.createNode(0, 0);
                final Component component1 = tm.createComponent(0, 0);
                final ComponentType typeDefinition1 = tm.createComponentType(0, 0);
                component1.setName("comp0");
                typeDefinition1.setName("td0");
                typeDefinition1.setVersion(1);
                component1.addTypeDefinition(typeDefinition1);
                node1.addComponents(component1);

                final SortedSet<AdaptationOperation> cb2 = nodeEngine.diff(node0, node1, platform).toBlocking().first();
                Assert.assertNotNull(cb2);
                final TreeSet<AdaptationOperation> expected = new TreeSet<>();
                assertThat(cb2).containsExactlyElementsOf(expected);
            }
        });
    }

    @Test
    public void testRemoveDeployUnitInPlatform() {
        final KevoreeModel tm = new KevoreeModel(DataManagerBuilder.create().withScheduler(new DirectScheduler()).build());
        tm.connect(new KCallback() {
            @Override
            public void on(Object cb) {


                final Node node0 = tm.createNode(0, 0);
                final Component component0 = tm.createComponent(0, 0);
                final ComponentType typeDefinition0 = tm.createComponentType(0, 0);
                final DeployUnit deployUnit0 = tm.createDeployUnit(0, 0);
                component0.setName("comp0");
                typeDefinition0.setName("td0");
                typeDefinition0.setVersion(1);
                deployUnit0.setName("du1");
                deployUnit0.setPlatform(platform);
                deployUnit0.setVersion("1.0.0");
                typeDefinition0.addDeployUnits(deployUnit0);
                component0.addTypeDefinition(typeDefinition0);
                node0.addComponents(component0);


                final Node node1 = tm.createNode(0, 0);
                final Component component1 = tm.createComponent(0, 0);
                final ComponentType typeDefinition1 = tm.createComponentType(0, 0);
                component1.setName("comp0");
                typeDefinition1.setName("td0");
                typeDefinition1.setVersion(1);
                component1.addTypeDefinition(typeDefinition1);
                node1.addComponents(component1);

                final SortedSet<AdaptationOperation> cb2 = nodeEngine.diff(node0, node1, platform).toBlocking().first();
                Assert.assertNotNull(cb2);
                final TreeSet<AdaptationOperation> expected = new TreeSet<>();
                expected.add(new RemoveDeployUnit(deployUnit0));
                assertThat(cb2).containsExactlyElementsOf(expected);
            }
        });
    }

    @Test
    public void testAddTwoComponentWithSameDUAndThenRemoveOnOfThem() {
        final KevoreeModel tm = new KevoreeModel(DataManagerBuilder.create().withScheduler(new DirectScheduler()).build());
        tm.connect(new KCallback() {
            @Override
            public void on(Object cb) {
                final Node node0 = tm.createNode(0, 0);
                final Component component00 = tm.createComponent(0, 0);
                final Component component01 = tm.createComponent(0, 0);
                final ComponentType typeDefintion0 = tm.createComponentType(0, 0);
                final DeployUnit deployUnit0 = tm.createDeployUnit(0, 0);
                component00.setName("comp0");
                component01.setName("comp1");
                typeDefintion0.setName("td0");
                typeDefintion0.setVersion(1);
                deployUnit0.setName("du0");
                deployUnit0.setPlatform(platform);
                deployUnit0.setVersion("1.0.0");
                typeDefintion0.addDeployUnits(deployUnit0);
                component00.addTypeDefinition(typeDefintion0);
                component01.addTypeDefinition(typeDefintion0);
                node0.addComponents(component00);
                node0.addComponents(component01);


                final Node node1 = tm.createNode(0, 0);
                final Component component10 = tm.createComponent(0, 0);
                final ComponentType typeDefintion1 = tm.createComponentType(0, 0);
                final DeployUnit deployUnit1 = tm.createDeployUnit(0, 0);
                component10.setName("comp0");
                typeDefintion1.setName("td0");
                typeDefintion1.setVersion(1);
                deployUnit1.setName("du0");
                deployUnit1.setPlatform(platform);
                deployUnit1.setVersion("1.0.0");
                typeDefintion1.addDeployUnits(deployUnit1);
                component10.addTypeDefinition(typeDefintion1);
                node1.addComponents(component10);

                final SortedSet<AdaptationOperation> cb2 = nodeEngine.diff(node0, node1, platform).toBlocking().first();
                Assert.assertNotNull(cb2);
                final TreeSet<AdaptationOperation> expected = new TreeSet<>();
                expected.add(new RemoveInstance(component01));
                assertThat(cb2).containsExactlyElementsOf(expected);
            }
        });
    }

    @Test
    public void testSelectLatestDeployUnit() {
        final KevoreeModel tm = new KevoreeModel(DataManagerBuilder.create().withScheduler(new DirectScheduler()).build());
        tm.connect(new KCallback() {
            @Override
            public void on(Object cb) {
                final Node node0 = tm.createNode(0, 0);

                final Node node1 = tm.createNode(0, 0);
                final Component component1 = tm.createComponent(0, 0);
                final ComponentType typeDefintion1 = tm.createComponentType(0, 0);
                final DeployUnit deployUnit1 = tm.createDeployUnit(0, 0);
                final DeployUnit deployUnit2 = tm.createDeployUnit(0, 0);
                component1.setName("comp0");
                typeDefintion1.setName("td0");
                typeDefintion1.setVersion(1);
                deployUnit1.setName("du0");
                deployUnit1.setPlatform(platform);
                deployUnit1.setVersion("1.0.0");
                typeDefintion1.addDeployUnits(deployUnit1);
                deployUnit2.setName("du0");
                deployUnit2.setPlatform(platform);
                deployUnit2.setVersion("2.0.0");
                typeDefintion1.addDeployUnits(deployUnit2);
                component1.addTypeDefinition(typeDefintion1);
                node1.addComponents(component1);

                final SortedSet<AdaptationOperation> cb2 = nodeEngine.diff(node0, node1, platform).toBlocking().first();
                Assert.assertNotNull(cb2);
                final TreeSet<AdaptationOperation> expected = new TreeSet<>();
                expected.add(new AddInstance(component1));
                expected.add(new AddDeployUnit(deployUnit2));
                assertThat(cb2).containsExactlyElementsOf(expected);
            }
        });
    }

    @Test
    public void testAddNewDeployUnitComponent() throws Exception {
        final KevoreeModel tm = new KevoreeModel(DataManagerBuilder.create().withScheduler(new DirectScheduler()).build());
        tm.connect(new KCallback() {
            @Override
            public void on(Object cb) {
                final Node node0 = tm.createNode(0, 0);
                final Component component0 = tm.createComponent(0, 0);
                final ComponentType componentType0 = tm.createComponentType(0, 0);
                final DeployUnit deployUnit00 = tm.createDeployUnit(0, 0);
                component0.setName("comp0");
                componentType0.setName("td0");
                componentType0.setVersion(1);
                deployUnit00.setPlatform(platform);
                deployUnit00.setName("du0");
                deployUnit00.setVersion("1.0.0");
                componentType0.addDeployUnits(deployUnit00);
                component0.addTypeDefinition(componentType0);
                node0.addComponents(component0);

                final Node node1 = tm.createNode(0, 0);
                final Component component1 = tm.createComponent(0, 0);
                final ComponentType componentType1 = tm.createComponentType(0, 0);
                final DeployUnit deployUnit10 = tm.createDeployUnit(0, 0);
                final DeployUnit deployUnit11 = tm.createDeployUnit(0, 0);
                component1.setName("comp0");
                componentType1.setName("td0");
                componentType1.setVersion(1);
                deployUnit10.setPlatform(platform);
                deployUnit10.setName("du0");
                deployUnit10.setVersion("1.0.0");
                deployUnit11.setPlatform(platform);
                deployUnit11.setName("du0");
                deployUnit11.setVersion("2.0.0");
                componentType1.addDeployUnits(deployUnit10);
                componentType1.addDeployUnits(deployUnit11);
                component1.addTypeDefinition(componentType1);
                node1.addComponents(component1);


                final SortedSet<AdaptationOperation> cb2 = nodeEngine.diff(node0, node1, platform).toBlocking().first();
                Assert.assertNotNull(cb2);
                final TreeSet<AdaptationOperation> expected = new TreeSet<>();
                expected.add(new RemoveInstance(component0));
                expected.add(new AddInstance(component1));
                expected.add(new RemoveDeployUnit(deployUnit00));
                expected.add(new AddDeployUnit(deployUnit11));
                assertThat(cb2).containsExactlyElementsOf(expected);
            }
        });
    }

    @Test
    public void testAddNewDeployUnitChannel() throws Exception {
        final KevoreeModel tm = new KevoreeModel(DataManagerBuilder.create().withScheduler(new DirectScheduler()).build());
        tm.connect(new KCallback() {
            @Override
            public void on(Object cb) {
                final Node node0 = tm.createNode(0, 0);
                final Channel chan0 = tm.createChannel(0, 0);
                final ChannelType channelType0 = tm.createChannelType(0, 0);
                final DeployUnit deployUnit00 = tm.createDeployUnit(0, 0);
                final Component component0 = tm.createComponent(0, 0);
                final InputPort inputPort0 = tm.createInputPort(0, 0);
                chan0.setName("comp0");
                channelType0.setName("td0");
                channelType0.setVersion(1);
                deployUnit00.setPlatform(platform);
                deployUnit00.setName("du0");
                deployUnit00.setVersion("1.0.0");
                channelType0.addDeployUnits(deployUnit00);
                chan0.addTypeDefinition(channelType0);
                inputPort0.addChannels(chan0);
                component0.addInputs(inputPort0);
                node0.addComponents(component0);

                final Node node1 = tm.createNode(0, 0);
                final Channel chan1 = tm.createChannel(0, 0);
                final ChannelType channelType1 = tm.createChannelType(0, 0);
                final DeployUnit deployUnit10 = tm.createDeployUnit(0, 0);
                final DeployUnit deployUnit11 = tm.createDeployUnit(0, 0);
                final Component component1 = tm.createComponent(0, 0);
                final InputPort inputPort1 = tm.createInputPort(0, 0);
                chan1.setName("comp0");
                channelType1.setName("td0");
                channelType1.setVersion(1);
                deployUnit10.setPlatform(platform);
                deployUnit10.setName("du0");
                deployUnit10.setVersion("1.0.0");
                deployUnit11.setPlatform(platform);
                deployUnit11.setName("du0");
                deployUnit11.setVersion("2.0.0");
                channelType1.addDeployUnits(deployUnit10);
                channelType1.addDeployUnits(deployUnit11);
                chan1.addTypeDefinition(channelType1);
                inputPort1.addChannels(chan1);
                component1.addInputs(inputPort1);
                node1.addComponents(component1);


                final SortedSet<AdaptationOperation> cb2 = nodeEngine.diff(node0, node1, platform).toBlocking().first();
                Assert.assertNotNull(cb2);
                final TreeSet<AdaptationOperation> expected = new TreeSet<>();
                expected.add(new RemoveDeployUnit(deployUnit00));
                expected.add(new AddDeployUnit(deployUnit11));
                expected.add(new RemoveInstance(chan0));
                expected.add(new AddInstance(chan1));
                assertThat(cb2).containsExactlyElementsOf(expected);
            }
        });
    }
}
