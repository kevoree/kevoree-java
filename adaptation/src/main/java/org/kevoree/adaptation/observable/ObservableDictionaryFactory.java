package org.kevoree.adaptation.observable;

import org.kevoree.Dictionary;
import org.kevoree.Param;
import org.kevoree.modeling.KCallback;
import rx.Observable;
import rx.Subscriber;

import java.util.Arrays;
import java.util.function.Consumer;

/**
 * Created by mleduc on 15/12/15.
 */
public class ObservableDictionaryFactory {

    public Observable<Param> getParamObservable(Dictionary x) {
        return Observable.create(new Observable.OnSubscribe<Param>() {
            @Override
            public void call(Subscriber<? super Param> subscriber) {
                x.getParams(new KCallback<Param[]>() {
                    @Override
                    public void on(Param[] cb) {
                        Arrays.asList(cb).forEach(new Consumer<Param>() {
                            @Override
                            public void accept(Param t) {
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
