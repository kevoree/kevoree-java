package org.kevoree.adaptation;

import org.kevoree.Node;
import org.kevoree.adaptation.operation.util.AdaptationOperation;
import org.kevoree.adaptation.business.DiffUtil;
import rx.Observable;
import rx.functions.Func2;

import java.util.SortedSet;

/**
 * Created by mleduc on 14/12/15.
 */
public class NodeEngine {

    private DiffUtil diffUtil = new DiffUtil();

    public Observable<SortedSet<AdaptationOperation>> diff(final Node before, final Node after, String platform) {
        final Observable<SortedSet<AdaptationOperation>> nodes = diffUtil.diffSubnodes(before, after);
        final Observable<SortedSet<AdaptationOperation>> components = diffUtil.diffComponents(before, after, platform);
        final Observable<SortedSet<AdaptationOperation>> dictionary = diffUtil.diffDictionary(before, after);
        final Observable<SortedSet<AdaptationOperation>> group = diffUtil.diffGroup(before, after);
        final Observable<SortedSet<AdaptationOperation>> channel = diffUtil.diffChannel(before, after);
        final Observable<SortedSet<AdaptationOperation>> status = diffUtil.diffInstanceStatus(before, after);
        final Observable<SortedSet<AdaptationOperation>> deployUnit = diffUtil.diffDeployUnit(before, after, platform);

        return Observable.merge(nodes, components, dictionary, group, channel, status, deployUnit).reduce(new Func2<SortedSet<AdaptationOperation>, SortedSet<AdaptationOperation>, SortedSet<AdaptationOperation>>() {
            @Override
            public SortedSet<AdaptationOperation> call(SortedSet<AdaptationOperation> set0, SortedSet<AdaptationOperation> set1) {
                set0.addAll(set1);
                return set0;
            }
        });
    }


}
