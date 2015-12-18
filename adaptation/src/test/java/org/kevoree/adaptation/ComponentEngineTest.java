package org.kevoree.adaptation;

import org.KevoreeModel;
import org.junit.Assert;
import org.junit.Test;
import org.kevoree.Channel;
import org.kevoree.Component;
import org.kevoree.Dictionary;
import org.kevoree.StringParam;
import org.kevoree.adaptation.operation.StartInstance;
import org.kevoree.adaptation.operation.StopInstance;
import org.kevoree.adaptation.operation.UpdateInstance;
import org.kevoree.adaptation.operation.UpdateParam;
import org.kevoree.adaptation.operation.util.AdaptationOperation;
import org.kevoree.modeling.KCallback;
import org.kevoree.modeling.memory.manager.DataManagerBuilder;
import org.kevoree.modeling.scheduler.impl.DirectScheduler;

import java.util.SortedSet;
import java.util.TreeSet;

import static org.assertj.core.api.Assertions.assertThat;

/**
 * Created by mleduc on 17/12/15.
 */
public class ComponentEngineTest {
    private final ComponentEngine componentEngine = new ComponentEngine();


    @Test
    public void testStart() throws Exception {
        final KevoreeModel tm = new KevoreeModel(DataManagerBuilder.create().withScheduler(new DirectScheduler()).build());
        tm.connect(new KCallback() {
            @Override
            public void on(Object cb) {
                final Component comp0 = tm.createComponent(0, 0);
                comp0.setStarted(false);
                final Component comp1 = tm.createComponent(0, 0);
                comp1.setStarted(true);
                final SortedSet<AdaptationOperation> stringStream = componentEngine.diff(comp0, comp1).toBlocking().first();
                Assert.assertNotNull(stringStream);
                final TreeSet<AdaptationOperation> expected = new TreeSet<>();
                expected.add(new StartInstance(comp1));
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
                final Component comp0 = tm.createComponent(0, 0);
                comp0.setStarted(true);
                final Component comp1 = tm.createComponent(0, 0);
                comp1.setStarted(false);
                final SortedSet<AdaptationOperation> stringStream = componentEngine.diff(comp0, comp1).toBlocking().first();
                Assert.assertNotNull(stringStream);
                final TreeSet<AdaptationOperation> expected = new TreeSet<>();
                expected.add(new StopInstance(comp1));
                assertThat(stringStream).containsExactlyElementsOf(expected);
            }
        });
    }

    @Test
    public void testUpdateBooleanParams() throws Exception {
        final KevoreeModel tm = new KevoreeModel(DataManagerBuilder.create().withScheduler(new DirectScheduler()).build());


        tm.connect(new KCallback() {
            @Override
            public void on(Object cb) {
                final Component comp0 = tm.createComponent(0, 0);
                final Dictionary dico0 = tm.createDictionary(0, 0);
                final StringParam param00 = tm.createStringParam(0, 0);
                final StringParam param01 = tm.createStringParam(0, 0);
                comp0.addDictionary(dico0);
                dico0.addParams(param00);
                dico0.addParams(param01);
                param00.setName("param0");
                param01.setName("param1");
                param00.setValue("A");
                param01.setValue("A");

                final Component comp1 = tm.createComponent(0, 0);
                final Dictionary dico1 = tm.createDictionary(0, 0);
                final StringParam param10 = tm.createStringParam(0, 0);
                final StringParam param11 = tm.createStringParam(0, 0);
                comp1.addDictionary(dico1);
                dico1.addParams(param10);
                dico1.addParams(param11);
                param10.setName("param0");
                param11.setName("param1");
                param10.setValue("B");
                param11.setValue("B");


                final SortedSet<AdaptationOperation> cb2 = componentEngine.diff(comp0, comp1).toBlocking().first();
                Assert.assertNotNull(cb2);
                final TreeSet<AdaptationOperation> expected = new TreeSet<>();
                expected.add(new UpdateParam(param10));
                expected.add(new UpdateParam(param11));
                expected.add(new UpdateInstance(comp1));
                assertThat(cb2).containsExactlyElementsOf(expected);
            }
        });
    }
}
