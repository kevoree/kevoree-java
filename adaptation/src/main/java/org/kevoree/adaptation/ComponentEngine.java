package org.kevoree.adaptation;

import org.kevoree.Component;
import org.kevoree.Node;
import org.kevoree.adaptation.operation.util.AdaptationOperation;
import org.kevoree.adaptation.util.DiffUtil;
import rx.Observable;
import rx.functions.Func2;

import java.util.SortedSet;

/**
 * Created by mleduc on 14/12/15.
 */
public class ComponentEngine {

    private DiffUtil diffUtil = new DiffUtil();

    public Observable<SortedSet<AdaptationOperation>> diff(final Component before, final Component after) {


        final Observable<SortedSet<AdaptationOperation>> dictionary = diffUtil.diffDictionary(before, after);
        final Observable<SortedSet<AdaptationOperation>> status = diffUtil.diffInstanceStatus(before, after);
        return Observable.merge(dictionary, status).reduce(new Func2<SortedSet<AdaptationOperation>, SortedSet<AdaptationOperation>, SortedSet<AdaptationOperation>>() {
            @Override
            public SortedSet<AdaptationOperation> call(SortedSet<AdaptationOperation> set0, SortedSet<AdaptationOperation> set1) {
                set0.addAll(set1);
                return set0;
            }
        });
    }


}
