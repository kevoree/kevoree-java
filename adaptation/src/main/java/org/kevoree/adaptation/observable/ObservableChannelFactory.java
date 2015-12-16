package org.kevoree.adaptation.observable;

import org.kevoree.Channel;
import org.kevoree.FragmentDictionary;
import org.kevoree.InputPort;
import org.kevoree.OutputPort;
import org.kevoree.adaptation.observable.util.ObservableDispatcher;
import rx.Observable;
import rx.Subscriber;

/**
 * Transform callback based access to channel related elements into Observable components.
 * <p>
 * Created by mleduc on 16/12/15.
 */
public class ObservableChannelFactory {
    public Observable<FragmentDictionary> getFragmentDictionaryObservable(final Channel chan) {
        return Observable.create(new Observable.OnSubscribe<FragmentDictionary>() {
            @Override
            public void call(Subscriber<? super FragmentDictionary> subscriber) {
                chan.getFragmentDictionary(new ObservableDispatcher<>(subscriber));
            }
        });
    }

    public Observable<InputPort> getInputObservable(final Channel chan) {
        return Observable.create(new Observable.OnSubscribe<InputPort>() {
            @Override
            public void call(Subscriber<? super InputPort> subscriber) {
                chan.getInputs(new ObservableDispatcher<>(subscriber));
            }
        });
    }

    public Observable<OutputPort> getOutputObservable(final Channel chan) {
        return Observable.create(new Observable.OnSubscribe<OutputPort>() {
            @Override
            public void call(Subscriber<? super OutputPort> subscriber) {
                chan.getOutputs(new ObservableDispatcher<>(subscriber));
            }
        });
    }
}
