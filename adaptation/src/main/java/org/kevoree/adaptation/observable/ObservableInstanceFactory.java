package org.kevoree.adaptation.observable;

import org.kevoree.Instance;
import org.kevoree.TypeDefinition;
import org.kevoree.modeling.KCallback;
import rx.Observable;
import rx.Subscriber;

import java.util.Arrays;
import java.util.function.Consumer;

/**
 * Created by mleduc on 15/12/15.
 */
public class ObservableInstanceFactory {


    public Observable<TypeDefinition> getTypeDefObservable(Instance n) {
        return Observable.create(new Observable.OnSubscribe<TypeDefinition>() {
            @Override
            public void call(Subscriber<? super TypeDefinition> subscriber) {
                n.getTypeDefinition(new KCallback<TypeDefinition[]>() {
                    @Override
                    public void on(TypeDefinition[] cb) {
                        Arrays.asList(cb).forEach(new Consumer<TypeDefinition>() {
                            @Override
                            public void accept(TypeDefinition t) {
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
