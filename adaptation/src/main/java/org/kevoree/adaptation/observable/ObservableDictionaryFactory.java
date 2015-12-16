package org.kevoree.adaptation.observable;

import org.kevoree.Dictionary;
import org.kevoree.Param;
import org.kevoree.adaptation.observable.util.ObservableDispatcher;
import rx.Observable;
import rx.Subscriber;

/**
 * Transform callback based access to dictionary related elements into Observable components.
 * <p>
 * Created by mleduc on 15/12/15.
 */
public class ObservableDictionaryFactory {

    public Observable<Param> getParamObservable(final Dictionary dico) {
        return Observable.create(new Observable.OnSubscribe<Param>() {
            @Override
            public void call(Subscriber<? super Param> subscriber) {
                dico.getParams(new ObservableDispatcher<>(subscriber));
            }
        });
    }
}
