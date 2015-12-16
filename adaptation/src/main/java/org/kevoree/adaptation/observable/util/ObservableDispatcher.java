package org.kevoree.adaptation.observable.util;

import org.kevoree.modeling.KCallback;
import rx.Subscriber;

/**
 * Send each element received from a callback one by one to an observer subscriber.
 * <p>
 * Created by mleduc on 16/12/15.
 */
public class ObservableDispatcher<T> implements KCallback<T[]> {
    private final Subscriber<? super T> subscriber;

    public ObservableDispatcher(Subscriber<? super T> subscriber) {
        this.subscriber = subscriber;
    }

    @Override
    public void on(T[] cb) {
        for (T a : cb) {
            subscriber.onNext(a);
        }

        if (!subscriber.isUnsubscribed()) {
            subscriber.onCompleted();
        }
    }
}
