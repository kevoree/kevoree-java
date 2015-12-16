package org.kevoree.adaptation.observable;

import org.kevoree.FragmentDictionary;
import org.kevoree.Param;
import org.kevoree.adaptation.observable.util.ObservableDispatcher;
import rx.Observable;
import rx.Subscriber;

/**
 * Created by mleduc on 15/12/15.
 */
public class ObservableFragmentDictionaryFactory {
    public Observable<Param> getParamObservable(final FragmentDictionary n) {
        return Observable.create(new Observable.OnSubscribe<Param>() {
            @Override
            public void call(Subscriber<? super Param> subscriber) {
                n.getParams(new ObservableDispatcher<>(subscriber));
            }
        });
    }


}
