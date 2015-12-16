package org.kevoree.adaptation.observable;

import org.kevoree.Item;
import org.kevoree.ListParam;
import org.kevoree.adaptation.observable.util.ObservableDispatcher;
import rx.Observable;
import rx.Subscriber;

/**
 * Transform callback based access to list param related elements into Observable components.
 * <p>
 * Created by mleduc on 16/12/15.
 */
public class ObservableListParamFactory {
    public Observable<Item> getValuesObservable(final ListParam listParam) {
        return rx.Observable.create(new Observable.OnSubscribe<Item>() {
            @Override
            public void call(Subscriber<? super Item> subscriber) {
                listParam.getValues(new ObservableDispatcher<>(subscriber));
            }
        });
    }
}
