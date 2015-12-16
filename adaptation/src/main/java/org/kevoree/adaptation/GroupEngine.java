package org.kevoree.adaptation;

import org.kevoree.Group;
import org.kevoree.adaptation.operation.util.AdaptationOperation;
import org.kevoree.adaptation.util.DiffUtil;
import rx.Observable;
import rx.functions.Func2;

import java.util.SortedSet;

/**
 * Created by mleduc on 16/12/15.
 */
public class GroupEngine {
    private DiffUtil diffUtil = new DiffUtil();

    public Observable<SortedSet<AdaptationOperation>> diff(final Group before, final Group after) {

        final Observable<SortedSet<AdaptationOperation>> dictionaryOperations = diffUtil.diffDictionary(before, after);
        final Observable<SortedSet<AdaptationOperation>> fragmentDictionaryOperations = diffUtil.diffFragmentDictionary(before, after);

        return Observable.merge(dictionaryOperations, fragmentDictionaryOperations).reduce(new Func2<SortedSet<AdaptationOperation>, SortedSet<AdaptationOperation>, SortedSet<AdaptationOperation>>() {
            @Override
            public SortedSet<AdaptationOperation> call(SortedSet<AdaptationOperation> strings, SortedSet<AdaptationOperation> strings2) {
                strings.addAll(strings2);
                return strings;
            }
        });
    }
}
