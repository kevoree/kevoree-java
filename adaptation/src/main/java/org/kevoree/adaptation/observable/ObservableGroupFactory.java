package org.kevoree.adaptation.observable;

import org.kevoree.FragmentDictionary;
import org.kevoree.Group;
import org.kevoree.adaptation.observable.util.ObservableDispatcher;
import rx.Observable;
import rx.Subscriber;

/**
 * Created by mleduc on 15/12/15.
 */
public class ObservableGroupFactory {
    public Observable<FragmentDictionary> getFragmentDictionaryObservable(final Group n) {
        return Observable.create(new Observable.OnSubscribe<FragmentDictionary>() {
            @Override
            public void call(Subscriber<? super FragmentDictionary> subscriber) {
                n.getFragmentDictionary(new ObservableDispatcher<>(subscriber));
            }
        });
    }
}
