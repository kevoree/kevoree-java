package org.kevoree.adaptation.business.comparators;

import org.kevoree.Component;
import org.kevoree.Node;
import org.kevoree.Port;
import org.kevoree.adaptation.business.DiffUtil;
import org.kevoree.adaptation.business.functional.Predicate;
import org.kevoree.adaptation.business.functional.PredicateFactory;
import org.kevoree.adaptation.observable.ObservableComponentFactory;
import org.kevoree.adaptation.observable.ObservablePortFactory;
import rx.Observable;
import rx.functions.Func2;

import java.util.Objects;

/**
 * Created by mleduc on 21/12/15.
 */
public class PortEquality implements PredicateFactory<Port> {

    private final ObservableComponentFactory observableComponentFactory;
    public ObservablePortFactory observablePortFactory;
    private final String platform;

    public PortEquality(ObservablePortFactory observablePortFactory, ObservableComponentFactory observableComponentFactory, String platform) {
        this.observablePortFactory = observablePortFactory;
        this.observableComponentFactory = observableComponentFactory;
        this.platform = platform;
    }

    @Override
    public Predicate<? super Port> get(final Port portA) {
        return new Predicate<Port>() {
            @Override
            public boolean test(Port portB) {
                final String nameA = portA.getName();
                final String nameB = portB.getName();
                final boolean ret;
                if (Objects.equals(nameA, nameB)) {
                    final Observable<Component> componentA = observablePortFactory.getComponentObservable(portA).first();
                    final Observable<Component> componentB = observablePortFactory.getComponentObservable(portB).first();
                    final Boolean res = Observable.zip(componentA, componentB, new Func2<Component, Component, Boolean>() {
                        @Override
                        public Boolean call(Component component, Component component2) {
                            final boolean ret;
                            if (ComponentEquality.componentEquality(component, component2, platform)) {
                                final Observable<Node> hostA = observableComponentFactory.getHostObservable(component);
                                final Observable<Node> hostB = observableComponentFactory.getHostObservable(component2);
                                final Boolean res = Observable.zip(hostA, hostB, new Func2<Node, Node, Boolean>() {
                                    @Override
                                    public Boolean call(Node node, Node node2) {
                                        final boolean res = NodeEquality.nodeEquality(node, node2);
                                        return res;
                                    }
                                }).toBlocking().first();
                                ret = res;
                            } else {
                                ret = false;
                            }
                            return ret;
                        }
                    }).toBlocking().first();
                    ret = res;
                } else {
                    ret = false;
                }

                return ret;
            }
        };
    }
}
