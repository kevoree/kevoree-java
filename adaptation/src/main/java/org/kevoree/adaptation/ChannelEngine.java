package org.kevoree.adaptation;

import org.kevoree.Channel;
import org.kevoree.Group;
import org.kevoree.adaptation.operation.util.AdaptationOperation;
import org.kevoree.adaptation.util.DiffUtil;
import rx.Observable;
import rx.functions.Func2;

import java.util.SortedSet;

/**
 * Created by mleduc on 16/12/15.
 */
public class ChannelEngine {
    private DiffUtil diffUtil = new DiffUtil();

    public Observable<SortedSet<AdaptationOperation>> diff(final Channel before, final Channel after) {

        final Observable<SortedSet<AdaptationOperation>> dictionaryOperations = diffUtil.diffDictionary(before, after);
        final Observable<SortedSet<AdaptationOperation>> fragmentDictionaryOperations = diffUtil.diffFragmentDictionary(before, after);
        final Observable<SortedSet<AdaptationOperation>> fragmentInputOperations = diffUtil.diffInput(before, after);
        final Observable<SortedSet<AdaptationOperation>> fragmentOutputOperations = diffUtil.diffOutput(before, after);

        return Observable.merge(dictionaryOperations, fragmentDictionaryOperations, fragmentInputOperations, fragmentOutputOperations).reduce(new Func2<SortedSet<AdaptationOperation>, SortedSet<AdaptationOperation>, SortedSet<AdaptationOperation>>() {
            @Override
            public SortedSet<AdaptationOperation> call(SortedSet<AdaptationOperation> set0, SortedSet<AdaptationOperation> set1) {
                set0.addAll(set1);
                return set0;
            }
        });
    }
}
