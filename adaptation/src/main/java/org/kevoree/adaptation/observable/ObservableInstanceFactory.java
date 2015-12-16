package org.kevoree.adaptation.observable;

import org.kevoree.Instance;
import org.kevoree.TypeDefinition;
import rx.Observable;

import java.util.Arrays;

/**
 * Created by mleduc on 15/12/15.
 */
public class ObservableInstanceFactory {


    public Observable<TypeDefinition> getTypeDefObservable(Instance n) {
        return Observable.create(subscriber -> n.getTypeDefinition(cb -> {
            Arrays.asList(cb).forEach(subscriber::onNext);
            if (!subscriber.isUnsubscribed()) {
                subscriber.onCompleted();
            }
        }));
    }
}
