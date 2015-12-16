package org.kevoree.adaptation.observable;

import org.kevoree.Dictionary;
import org.kevoree.Param;
import org.kevoree.adaptation.observable.util.ObservableDispatcher;
import rx.Observable;
import rx.Subscriber;

/**
 * Created by mleduc on 15/12/15.
 */
public class ObservableDictionaryFactory {

    public Observable<Param> getParamObservable(final Dictionary x) {
        return Observable.create(new Observable.OnSubscribe<Param>() {
            @Override
            public void call(Subscriber<? super Param> subscriber) {
                x.getParams(new ObservableDispatcher<>(subscriber));
            }
        });
    }
}
