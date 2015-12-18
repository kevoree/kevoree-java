package org.kevoree.adaptation;

import org.KevoreeModel;
import org.junit.Assert;
import org.junit.Test;
import org.kevoree.*;
import org.kevoree.adaptation.operation.AddInstance;
import org.kevoree.adaptation.operation.RemoveInstance;
import org.kevoree.adaptation.operation.UpdateInstance;
import org.kevoree.adaptation.operation.UpdateParam;
import org.kevoree.adaptation.operation.util.AdaptationOperation;
import org.kevoree.modeling.KCallback;
import org.kevoree.modeling.memory.manager.DataManagerBuilder;
import org.kevoree.modeling.scheduler.impl.DirectScheduler;

import java.util.Set;
import java.util.SortedSet;
import java.util.TreeSet;

import static org.assertj.core.api.Assertions.assertThat;

/**
 * Created by mleduc on 17/12/15.
 */
public class ChannelEngineTest {

    private final ChannelEngine channelEngine = new ChannelEngine();

    @Test
    public void test() throws Exception {
        final KevoreeModel tm = new KevoreeModel(DataManagerBuilder.create().withScheduler(new DirectScheduler()).build());
        tm.connect(new KCallback() {
            @Override
            public void on(Object cb) {
                final Channel chan0 = tm.createChannel(0, 0);
                final Channel chan1 = tm.createChannel(0, 0);
                final SortedSet<AdaptationOperation> stringStream = channelEngine.diff(chan0, chan1).toBlocking().first();
                Assert.assertNotNull(stringStream);
                Assert.assertEquals(0, stringStream.size());
            }
        });
    }

    @Test
    public void dictionnaryTestChannelNothingChanged() throws Exception {
        final KevoreeModel tm = new KevoreeModel(DataManagerBuilder.create().withScheduler(new DirectScheduler()).build());
        tm.connect(new KCallback() {
            @Override
            public void on(Object cb) {

                // init chan0
                final Channel chan0 = tm.createChannel(0, 0);
                final Dictionary dictionnary0 = tm.createDictionary(0, 0);
                final NumberParam param01 = tm.createNumberParam(0, 0);
                param01.setName("p1");
                param01.setValue("11");
                dictionnary0.addParams(param01);
                final BooleanParam param02 = tm.createBooleanParam(0, 0);
                param02.setName("p2");
                param02.setValue(true);
                dictionnary0.addParams(param02);
                chan0.addDictionary(dictionnary0);

                // init chan1
                final Channel chan1 = tm.createChannel(0, 0);
                final Dictionary dictionnary1 = tm.createDictionary(0, 0);
                final NumberParam param11 = tm.createNumberParam(0, 0);
                param11.setName("p1");
                param11.setValue("11");
                dictionnary1.addParams(param11);
                final BooleanParam param12 = tm.createBooleanParam(0, 0);
                param12.setName("p2");
                param12.setValue(true);
                dictionnary1.addParams(param12);
                chan1.addDictionary(dictionnary1);
                final SortedSet<AdaptationOperation> res = channelEngine.diff(chan0, chan1).toBlocking().first();
                Assert.assertNotNull(res);
                Assert.assertEquals(0, res.size());
            }
        });
    }

    @Test
    public void dictionnaryTestChannelParamsValuesChanged() throws Exception {
        final KevoreeModel tm = new KevoreeModel(DataManagerBuilder.create().withScheduler(new DirectScheduler()).build());
        tm.connect(new KCallback() {
            @Override
            public void on(Object cb) {

                // init chan0
                final Channel chan0 = tm.createChannel(0, 0);
                final Dictionary dictionnary0 = tm.createDictionary(0, 0);
                final NumberParam param01 = tm.createNumberParam(0, 0);
                param01.setName("p1");
                param01.setValue("11");
                dictionnary0.addParams(param01);
                final BooleanParam param02 = tm.createBooleanParam(0, 0);
                param02.setName("p2");
                param02.setValue(true);
                dictionnary0.addParams(param02);
                chan0.addDictionary(dictionnary0);

                // init chan1
                final Channel chan1 = tm.createChannel(0, 0);
                final Dictionary dictionnary1 = tm.createDictionary(0, 0);
                final NumberParam param11 = tm.createNumberParam(0, 0);
                param11.setName("p1");
                param11.setValue("12");
                dictionnary1.addParams(param11);
                final BooleanParam param12 = tm.createBooleanParam(0, 0);
                param12.setName("p2");
                param12.setValue(false);
                dictionnary1.addParams(param12);
                chan1.addDictionary(dictionnary1);
                final SortedSet<AdaptationOperation> res = channelEngine.diff(chan0, chan1).toBlocking().first();
                Assert.assertNotNull(res);
                final TreeSet<AdaptationOperation> expected = new TreeSet<>();
                expected.add(new UpdateParam(param11.uuid()));
                expected.add(new UpdateParam(param12.uuid()));
                expected.add(new UpdateInstance(chan1.uuid()));
                assertThat(res).containsExactlyElementsOf(expected);
            }
        });
    }

    /**
     * Advanced test:
     * <p>
     * In a first time we will create a Model with 1 nodes node0 and node1.
     * both hosts two components one of type Printer, which offer an input port named "input" and one of type Ticker, which offer an output port name "tick".
     * <p>
     * The model also contains two a channel chan0.
     * <p>
     * In a first time the components are not connected to chan0.
     * In a second time all the component are connected to chan0.
     * <p>
     * The 4 connections should be detected.
     *
     * @throws Exception
     */
    @Test
    public void testAddTwoNewInputs() throws Exception {
        final KevoreeModel tm = new KevoreeModel(DataManagerBuilder.create().withScheduler(new DirectScheduler()).build());
        tm.connect(new KCallback() {
            @Override
            public void on(Object cb) {
                final Channel chan0 = tm.createChannel(0, 0);

                final Channel chan1 = tm.createChannel(0, 1);
                final InputPort inputPortPrinter00 = tm.createInputPort(0, 1);
                inputPortPrinter00.setName("input");
                chan1.addInputs(inputPortPrinter00);
                final InputPort inputPortPrinter10 = tm.createInputPort(0, 1);
                inputPortPrinter10.setName("input");
                chan1.addInputs(inputPortPrinter10);
                final OutputPort outputPortTicker00 = tm.createOutputPort(0, 1);
                outputPortTicker00.setName("tick");
                chan1.addOutputs(outputPortTicker00);
                final OutputPort outputPortTicker10 = tm.createOutputPort(0, 1);
                outputPortTicker10.setName("tick");
                chan1.addOutputs(outputPortTicker10);
                final Node node0 = tm.createNode(0, 1);
                final Component ticker00 = tm.createComponent(0, 1);
                ticker00.setName("ticker0");
                ticker00.addOutputs(outputPortTicker00);
                node0.addComponents(ticker00);
                final Component printer00 = tm.createComponent(0, 1);
                printer00.setName("printer0");
                printer00.addInputs(inputPortPrinter00);
                node0.addComponents(printer00);

                final Node node1 = tm.createNode(0, 1);
                final Component ticker10 = tm.createComponent(0, 1);
                ticker10.setName("ticker0");
                ticker10.addOutputs(outputPortTicker10);
                node1.addComponents(ticker10);
                final Component printer10 = tm.createComponent(0, 1);
                printer10.setName("printer0");
                printer10.addInputs(inputPortPrinter10);
                node1.addComponents(printer10);

                final SortedSet<AdaptationOperation> res = channelEngine.diff(chan0, chan1).toBlocking().first();
                Assert.assertNotNull(res);
                final TreeSet<AdaptationOperation> expected = new TreeSet<>();
                expected.add(new AddInstance(inputPortPrinter00.uuid()));
                expected.add(new AddInstance(inputPortPrinter10.uuid()));
                expected.add(new AddInstance(outputPortTicker00.uuid()));
                expected.add(new AddInstance(outputPortTicker10.uuid()));
                assertThat(res).containsExactlyElementsOf(expected);
            }
        });
    }

    @Test
    public void removeAllInput() throws Exception {
        final KevoreeModel tm = new KevoreeModel(DataManagerBuilder.create().withScheduler(new DirectScheduler()).build());
        tm.connect(new KCallback() {
            @Override
            public void on(Object cb) {

                final Channel chan0 = tm.createChannel(0, 1);
                final InputPort inputPortPrinter00 = tm.createInputPort(0, 1);
                final InputPort inputPortPrinter01 = tm.createInputPort(0, 1);
                final OutputPort outputPortTicker00 = tm.createOutputPort(0, 1);
                final OutputPort outputPortTicker01 = tm.createOutputPort(0, 1);
                final Node node00 = tm.createNode(0, 1);
                final Node node01 = tm.createNode(0, 1);
                final Component ticker00 = tm.createComponent(0, 1);
                final Component ticker01 = tm.createComponent(0, 1);
                final Component printer00 = tm.createComponent(0, 1);
                final Component printer01 = tm.createComponent(0, 1);


                node00.setName("node0");
                node01.setName("node1");
                inputPortPrinter00.setName("input");
                chan0.addInputs(inputPortPrinter00);
                inputPortPrinter01.setName("input");
                chan0.addInputs(inputPortPrinter01);
                outputPortTicker00.setName("tick");
                chan0.addOutputs(outputPortTicker00);
                outputPortTicker01.setName("tick");
                chan0.addOutputs(outputPortTicker01);
                ticker00.setName("ticker0");
                ticker00.addOutputs(outputPortTicker00);
                node00.addComponents(ticker00);
                printer00.setName("printer0");
                printer00.addInputs(inputPortPrinter00);
                node00.addComponents(printer00);
                ticker01.setName("ticker0");
                ticker01.addOutputs(outputPortTicker01);
                node01.addComponents(ticker01);
                printer01.setName("printer0");
                printer01.addInputs(inputPortPrinter01);
                node01.addComponents(printer01);

                final Channel chan1 = tm.createChannel(0, 1);
                final InputPort inputPortPrinter11 = tm.createInputPort(0, 1);
                final OutputPort outputPortTicker11 = tm.createOutputPort(0,1);
                final Node node10 = tm.createNode(0, 1);
                final Node node11 = tm.createNode(0, 1);
                final Component ticker10 = tm.createComponent(0, 1);
                final Component ticker11 = tm.createComponent(0, 1);
                final Component printer10 = tm.createComponent(0, 1);
                final Component printer11 = tm.createComponent(0, 1);
                node10.setName("node0");
                node11.setName("node1");

                inputPortPrinter11.setName("input");
                chan1.addInputs(inputPortPrinter11);
                chan1.addOutputs(outputPortTicker11);
                ticker10.setName("ticker0");
                node10.addComponents(ticker10);
                printer10.setName("printer0");
                node10.addComponents(printer10);
                ticker11.setName("ticker0");
                outputPortTicker11.setName("tick");
                ticker11.addOutputs(outputPortTicker11);
                node11.addComponents(ticker11);
                printer11.setName("printer0");
                printer11.addInputs(inputPortPrinter11);
                node11.addComponents(printer11);



                final SortedSet<AdaptationOperation> res = channelEngine.diff(chan0, chan1).toBlocking().first();
                Assert.assertNotNull(res);
                final TreeSet<AdaptationOperation> expected = new TreeSet<>();
                expected.add(new RemoveInstance(inputPortPrinter00.uuid()));
                expected.add(new RemoveInstance(outputPortTicker00.uuid()));
                assertThat(res).containsExactlyElementsOf(expected);
            }
        });
    }

    @Test
    public void testUpdatedFragmentParam() throws Exception {
        final KevoreeModel tm = new KevoreeModel(DataManagerBuilder.create().withScheduler(new DirectScheduler()).build());
        tm.connect(new KCallback() {
            @Override
            public void on(Object cb) {
                final Channel channel0 = tm.createChannel(0,0);
                final FragmentDictionary fragmentDictionary0 = tm.createFragmentDictionary(0, 0);
                final StringParam stringParam0 = tm.createStringParam(0, 0);
                stringParam0.setName("sp0");
                stringParam0.setValue("0");
                fragmentDictionary0.addParams(stringParam0);
                channel0.addFragmentDictionary(fragmentDictionary0);

                final Channel channel1 = tm.createChannel(0,0);
                final FragmentDictionary fragmentDictionary1 = tm.createFragmentDictionary(0, 0);
                final StringParam stringParam1 = tm.createStringParam(0, 0);
                stringParam1.setName("sp0");
                stringParam1.setValue("A");
                fragmentDictionary1.addParams(stringParam1);
                channel1.addFragmentDictionary(fragmentDictionary1);

                final SortedSet<AdaptationOperation> res = channelEngine.diff(channel0, channel1).toBlocking().first();
                Assert.assertNotNull(res);
                final Set<AdaptationOperation> expected = new TreeSet<>();
                expected.add(new UpdateParam(stringParam1.uuid()));
                expected.add(new UpdateInstance(channel1.uuid()));
                assertThat(res).containsExactlyElementsOf(expected);
            }
        });
    }

}
