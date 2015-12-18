package org.kevoree.adaptation;

import org.KevoreeModel;
import org.junit.Assert;
import org.junit.Test;
import org.kevoree.*;
import org.kevoree.adaptation.operation.StartInstance;
import org.kevoree.adaptation.operation.StopInstance;
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
public class GroupEngineTest {
    private final GroupEngine groupEngine = new GroupEngine();

    @Test
    public void testStart() throws Exception {
        final KevoreeModel tm = new KevoreeModel(DataManagerBuilder.create().withScheduler(new DirectScheduler()).build());
        tm.connect(new KCallback() {
            @Override
            public void on(Object cb) {
                final Group group0 = tm.createGroup(0,0);
                group0.setStarted(false);
                final Group group1 = tm.createGroup(0,0);
                group1.setStarted(true);
                final SortedSet<AdaptationOperation> stringStream = groupEngine.diff(group0, group1).toBlocking().first();
                Assert.assertNotNull(stringStream);
                final TreeSet<AdaptationOperation> expected = new TreeSet<>();
                expected.add(new StartInstance(group1));
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
                final Group group0 = tm.createGroup(0,0);
                group0.setStarted(true);
                final Group group1 = tm.createGroup(0,0);
                group1.setStarted(false);
                final SortedSet<AdaptationOperation> stringStream = groupEngine.diff(group0, group1).toBlocking().first();
                Assert.assertNotNull(stringStream);
                final TreeSet<AdaptationOperation> expected = new TreeSet<>();
                expected.add(new StopInstance(group1));
                assertThat(stringStream).containsExactlyElementsOf(expected);
            }
        });
    }

    @Test
    public void testUpdatedFragmentParam() throws Exception {
        final KevoreeModel tm = new KevoreeModel(DataManagerBuilder.create().withScheduler(new DirectScheduler()).build());
        tm.connect(new KCallback() {
            @Override
            public void on(Object cb) {
                final Group group0 = tm.createGroup(0,0);
                final FragmentDictionary fragmentDictionary0 = tm.createFragmentDictionary(0, 0);
                final StringParam stringParam0 = tm.createStringParam(0, 0);
                stringParam0.setName("sp0");
                stringParam0.setValue("0");
                fragmentDictionary0.addParams(stringParam0);
                group0.addFragmentDictionary(fragmentDictionary0);

                final Group group1 = tm.createGroup(0,0);
                final FragmentDictionary fragmentDictionary1 = tm.createFragmentDictionary(0, 0);
                final StringParam stringParam1 = tm.createStringParam(0, 0);
                stringParam1.setName("sp0");
                stringParam1.setValue("A");
                fragmentDictionary1.addParams(stringParam1);
                group1.addFragmentDictionary(fragmentDictionary1);
                final SortedSet<AdaptationOperation> res = groupEngine.diff(group0, group1).toBlocking().first();
                Assert.assertNotNull(res);
                final Set<AdaptationOperation> expected = new TreeSet<>();
                expected.add(new UpdateParam(stringParam1));
                expected.add(new UpdateInstance(group1));
                assertThat(res).containsExactlyElementsOf(expected);
            }
        });
    }

}
