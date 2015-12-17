package org.kevoree.adaptation.observable;

import org.kevoree.*;
import org.kevoree.adaptation.observable.util.ObservableDispatcher;
import rx.Observable;
import rx.Subscriber;

/**
 * Created by mleduc on 16/12/15.
 */
public class ObservablePortFactory {
    public Observable<Channel> getChannelObservable(final Port port) {
        return Observable.create(new Observable.OnSubscribe<Channel>() {
            @Override
            public void call(Subscriber<? super Channel> subscriber) {
                port.getChannels(new ObservableDispatcher<>(subscriber));
            }
        });
    }

    public Observable<Component> getComponentObservable(final Port port) {
        return Observable.create(new Observable.OnSubscribe<Component>() {
            @Override
            public void call(Subscriber<? super Component> subscriber) {
                port.getComponents(new ObservableDispatcher<>(subscriber));
            }
        });
    }
}
