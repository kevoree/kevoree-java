package org.kevoree.adaptation.observable;

import org.kevoree.FragmentDictionary;
import org.kevoree.Group;
import org.kevoree.adaptation.observable.util.ObservableDispatcher;
import rx.Observable;
import rx.Subscriber;

/**
 * Transform callback based access to group related elements into Observable components.
 * <p>
 * Created by mleduc on 15/12/15.
 */
public class ObservableGroupFactory {
    public Observable<FragmentDictionary> getFragmentDictionaryObservable(final Group group) {
        return Observable.create(new Observable.OnSubscribe<FragmentDictionary>() {
            @Override
            public void call(Subscriber<? super FragmentDictionary> subscriber) {
                group.getFragmentDictionary(new ObservableDispatcher<>(subscriber));
            }
        });
    }
}
