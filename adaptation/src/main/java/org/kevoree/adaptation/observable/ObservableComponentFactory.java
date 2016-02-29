package org.kevoree.adaptation.observable;

import org.kevoree.*;
import org.kevoree.adaptation.observable.util.ObservableDispatcher;
import rx.Observable;
import rx.Subscriber;

/**
 * Created by mleduc on 16/12/15.
 */
public class ObservableComponentFactory {
    public Observable<InputPort> getInputPortFactory(final Component component) {
        return Observable.create(new Observable.OnSubscribe<InputPort>() {
            @Override
            public void call(Subscriber<? super InputPort> subscriber) {
                component.getInputs(new ObservableDispatcher<>(subscriber));
            }
        });
    }

    public Observable<OutputPort> getOutputPortFactory(final Component component) {
        return Observable.create(new Observable.OnSubscribe<OutputPort>() {
            @Override
            public void call(Subscriber<? super OutputPort> subscriber) {
                component.getOutputs(new ObservableDispatcher<>(subscriber));
            }
        });
    }

    public Observable<Node> getHostObservable(final Component component) {
        return Observable.create(new Observable.OnSubscribe<Node>() {
            @Override
            public void call(Subscriber<? super Node> subscriber) {
                component.getHost(new ObservableDispatcher<>(subscriber));
            }
        });
    }
}
