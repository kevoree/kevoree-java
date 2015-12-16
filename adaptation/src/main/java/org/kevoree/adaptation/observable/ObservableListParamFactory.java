package org.kevoree.adaptation.observable;

import org.kevoree.Item;
import org.kevoree.ListParam;
import rx.Observable;

import java.util.Arrays;

/**
 * Created by mleduc on 16/12/15.
 */
public class ObservableListParamFactory {
    public Observable<Item> getValuesObservable(ListParam p) {
        return rx.Observable.create(subscriber -> p.getValues(cb -> {
            Arrays.asList(cb).forEach(subscriber::onNext);
            if (!subscriber.isUnsubscribed()) {
                subscriber.onCompleted();
            }
        }));
    }
}
