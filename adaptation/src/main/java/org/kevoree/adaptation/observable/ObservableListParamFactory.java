package org.kevoree.adaptation.observable;

import org.kevoree.Item;
import org.kevoree.ListParam;
import org.kevoree.modeling.KCallback;
import rx.Observable;
import rx.Subscriber;

import java.util.Arrays;
import java.util.function.Consumer;

/**
 * Created by mleduc on 16/12/15.
 */
public class ObservableListParamFactory {
    public Observable<Item> getValuesObservable(ListParam p) {
        return rx.Observable.create(new Observable.OnSubscribe<Item>() {
            @Override
            public void call(Subscriber<? super Item> subscriber) {
                p.getValues(new KCallback<Item[]>() {
                    @Override
                    public void on(Item[] cb) {
                        Arrays.asList(cb).forEach(new Consumer<Item>() {
                            @Override
                            public void accept(Item t) {
                                subscriber.onNext(t);
                            }
                        });
                        if (!subscriber.isUnsubscribed()) {
                            subscriber.onCompleted();
                        }
                    }
                });
            }
        });
    }
}
