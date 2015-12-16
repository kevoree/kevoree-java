package org.kevoree.adaptation.observable;

import org.kevoree.Dictionary;
import org.kevoree.Param;
import rx.Observable;

import java.util.Arrays;

/**
 * Created by mleduc on 15/12/15.
 */
public class ObservableDictionaryFactory {

    public Observable<Param> getParamObservable(Dictionary x) {
        return Observable.create(subscriber -> x.getParams(cb -> {
            Arrays.asList(cb).forEach(subscriber::onNext);
            if (!subscriber.isUnsubscribed()) {
                subscriber.onCompleted();
            }
        }));
    }
}
