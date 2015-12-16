package org.kevoree.adaptation.observable;

import org.kevoree.Channel;
import org.kevoree.FragmentDictionary;
import org.kevoree.adaptation.observable.util.ObservableDispatcher;
import rx.Observable;
import rx.Subscriber;

/**
 * Created by mleduc on 16/12/15.
 */
public class ObservableChannelFactory {
    public Observable<FragmentDictionary> getFragmentDictionaryObservable(final Channel n) {
        return Observable.create(new Observable.OnSubscribe<FragmentDictionary>() {
            @Override
            public void call(Subscriber<? super FragmentDictionary> subscriber) {
                n.getFragmentDictionary(new ObservableDispatcher<>(subscriber));
            }
        });
    }
}
