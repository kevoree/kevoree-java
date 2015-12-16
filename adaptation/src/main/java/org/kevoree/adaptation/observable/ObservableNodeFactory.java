package org.kevoree.adaptation.observable;

import org.kevoree.Component;
import org.kevoree.Dictionary;
import org.kevoree.Group;
import org.kevoree.Node;
import org.kevoree.modeling.KCallback;
import rx.Observable;
import rx.Subscriber;

import java.util.Arrays;
import java.util.function.Consumer;

/**
 * Created by mleduc on 15/12/15.
 */
public class ObservableNodeFactory {
    public Observable<Component> getComponentObservable(Node n) {
        return Observable.create(new Observable.OnSubscribe<Component>() {
            @Override
            public void call(Subscriber<? super Component> subscriber) {
                n.getComponents(new KCallback<Component[]>() {
                    @Override
                    public void on(Component[] cb) {
                        Arrays.asList(cb).forEach(new Consumer<Component>() {
                            @Override
                            public void accept(Component t) {
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

    public Observable<Node> getSubnodeObservable(Node n) {
        return Observable.create(new Observable.OnSubscribe<Node>() {
            @Override
            public void call(Subscriber<? super Node> subscriber) {
                n.getSubNodes(new KCallback<Node[]>() {
                    @Override
                    public void on(Node[] cb) {
                        Arrays.asList(cb).forEach(new Consumer<Node>() {
                            @Override
                            public void accept(Node t) {
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

    public Observable<Dictionary> getDictionaryObservable(Node n) {
        return Observable.create(new Observable.OnSubscribe<Dictionary>() {
            @Override
            public void call(Subscriber<? super Dictionary> subscriber) {
                n.getDictionary(new KCallback<Dictionary[]>() {
                    @Override
                    public void on(Dictionary[] cb) {
                        Arrays.asList(cb).forEach(new Consumer<Dictionary>() {
                            @Override
                            public void accept(Dictionary t) {
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

    public Observable<Group> getGroupObservable(Node n) {
        return Observable.create(new Observable.OnSubscribe<Group>() {
            @Override
            public void call(Subscriber<? super Group> subscriber) {
                n.getGroups(new KCallback<Group[]>() {
                    @Override
                    public void on(Group[] cb) {
                        Arrays.asList(cb).forEach(new Consumer<Group>() {
                            @Override
                            public void accept(Group t) {
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
