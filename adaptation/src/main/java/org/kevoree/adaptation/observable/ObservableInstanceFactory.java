package org.kevoree.adaptation.observable;

import org.kevoree.Dictionary;
import org.kevoree.Instance;
import org.kevoree.TypeDefinition;
import org.kevoree.adaptation.observable.util.ObservableDispatcher;
import rx.Observable;
import rx.Subscriber;

/**
 * Transform callback based access to instance related elements into Observable components.
 * <p>
 * Created by mleduc on 15/12/15.
 */
public class ObservableInstanceFactory {

    public Observable<TypeDefinition> getTypeDefObservable(final Instance instance) {
        return Observable.create(new Observable.OnSubscribe<TypeDefinition>() {
            @Override
            public void call(Subscriber<? super TypeDefinition> subscriber) {
                instance.getTypeDefinition(new ObservableDispatcher<>(subscriber));
            }
        });
    }

    public Observable<Dictionary> getDictionaryObservable(final Instance instance) {
        return Observable.create(new Observable.OnSubscribe<Dictionary>() {
            @Override
            public void call(Subscriber<? super Dictionary> subscriber) {
                instance.getDictionary(new ObservableDispatcher<>(subscriber));
            }
        });

    }
}
