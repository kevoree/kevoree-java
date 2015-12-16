package org.kevoree.adaptation.observable;

import org.kevoree.Item;
import org.kevoree.ListParam;
import org.kevoree.adaptation.observable.util.ObservableDispatcher;
import rx.Observable;
import rx.Subscriber;

/**
 * Created by mleduc on 16/12/15.
 */
public class ObservableListParamFactory {
    public Observable<Item> getValuesObservable(final ListParam p) {
        return rx.Observable.create(new Observable.OnSubscribe<Item>() {
            @Override
            public void call(Subscriber<? super Item> subscriber) {
                p.getValues(new ObservableDispatcher<>(subscriber));
            }
        });
    }
}
