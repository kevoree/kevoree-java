package org.kevoree.adaptation.observable;

import org.kevoree.Component;
import org.kevoree.Dictionary;
import org.kevoree.Group;
import org.kevoree.Node;
import rx.Observable;

import java.util.Arrays;

/**
 * Created by mleduc on 15/12/15.
 */
public class ObservableNodeFactory {
    public Observable<Component> getComponentObservable(Node n) {
        return Observable.create(subscriber -> n.getComponents(cb -> {
            Arrays.asList(cb).forEach(subscriber::onNext);
            if (!subscriber.isUnsubscribed()) {
                subscriber.onCompleted();
            }
        }));
    }

    public Observable<Node> getSubnodeObservable(Node n) {
        return Observable.create(subscriber -> n.getSubNodes(cb -> {
            Arrays.asList(cb).forEach(subscriber::onNext);
            if (!subscriber.isUnsubscribed()) {
                subscriber.onCompleted();
            }
        }));
    }

    public Observable<Dictionary> getDictionaryObservable(Node n) {
        return Observable.create(subscriber -> n.getDictionary(cb -> {
            Arrays.asList(cb).forEach(subscriber::onNext);
            if (!subscriber.isUnsubscribed()) {
                subscriber.onCompleted();
            }
        }));

    }

    public Observable<Group> getGroupObservable(Node n) {
        return Observable.create(subscriber -> n.getGroups(cb -> {
            Arrays.asList(cb).forEach(subscriber::onNext);
            if (!subscriber.isUnsubscribed()) {
                subscriber.onCompleted();
            }
        }));
    }

}
