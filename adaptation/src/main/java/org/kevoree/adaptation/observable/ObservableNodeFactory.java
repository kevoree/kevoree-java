package org.kevoree.adaptation.observable;

import org.kevoree.Component;
import org.kevoree.Group;
import org.kevoree.Node;
import org.kevoree.adaptation.observable.util.ObservableDispatcher;
import org.kevoree.adaptation.util.Consumer;
import rx.Observable;
import rx.Subscriber;


/**
 * Created by mleduc on 15/12/15.
 */
public class ObservableNodeFactory {
    public Observable<Component> getComponentObservable(final Node n) {
        return Observable.create(new Observable.OnSubscribe<Component>() {
            @Override
            public void call(Subscriber<? super Component> subscriber) {
                n.getComponents(new ObservableDispatcher<>(subscriber));
            }
        });
    }

    public Observable<Node> getSubnodeObservable(final Node n) {
        return Observable.create(new Observable.OnSubscribe<Node>() {
            @Override
            public void call(Subscriber<? super Node> subscriber) {
                n.getSubNodes(new ObservableDispatcher<>(subscriber));
            }
        });
    }


    public Observable<Group> getGroupObservable(final Node n) {
        return Observable.create(new Observable.OnSubscribe<Group>() {
            @Override
            public void call(Subscriber<? super Group> subscriber) {
                final ObservableDispatcher<Group> cb = new ObservableDispatcher<>(subscriber);
                final Consumer<ObservableDispatcher<Group>> consumer = new Consumer<ObservableDispatcher<Group>>() {

                    @Override
                    public void accept(ObservableDispatcher<Group> group) {
                        n.getGroups(cb);
                    }
                };
                consumer.accept(cb);
            }
        });
    }

}
