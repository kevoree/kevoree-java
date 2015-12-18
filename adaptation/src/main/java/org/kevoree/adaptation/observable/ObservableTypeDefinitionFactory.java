package org.kevoree.adaptation.observable;

import org.kevoree.*;
import org.kevoree.adaptation.observable.util.ObservableDispatcher;
import rx.Observable;
import rx.Subscriber;

/**
 * Transform callback based access to channel related elements into Observable components.
 * <p>
 * Created by mleduc on 16/12/15.
 */
public class ObservableTypeDefinitionFactory {
    public Observable<DeployUnit> getDeployUnitObservable(final TypeDefinition chan) {
        return Observable.create(new Observable.OnSubscribe<DeployUnit>() {
            @Override
            public void call(Subscriber<? super DeployUnit> subscriber) {
                chan.getDeployUnits(new ObservableDispatcher<>(subscriber));
            }
        });
    }

}
