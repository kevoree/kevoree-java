package org.kevoree.adaptation;

import org.kevoree.*;
import org.kevoree.adaptation.observable.ObservableDictionaryFactory;
import org.kevoree.adaptation.observable.ObservableInstanceFactory;
import org.kevoree.adaptation.observable.ObservableListParamFactory;
import org.kevoree.adaptation.observable.ObservableNodeFactory;
import org.kevoree.adaptation.operation.util.AdaptationOperation;
import org.kevoree.adaptation.operation.AddInstance;
import org.kevoree.adaptation.operation.RemoveInstance;
import org.kevoree.adaptation.operation.UpdateInstance;
import rx.Observable;
import rx.functions.Func2;

import java.util.*;
import java.util.function.Function;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import static java.util.stream.Stream.concat;

/**
 * Created by mleduc on 14/12/15.
 */
public class NodeEngine {

    private final ObservableNodeFactory observableNodeFactory = new ObservableNodeFactory();
    private final ObservableDictionaryFactory observableDictionaryFactory = new ObservableDictionaryFactory();
    private final ObservableInstanceFactory observableInstanceFactory = new ObservableInstanceFactory();
    private final ObservableListParamFactory observableListParamFactory = new ObservableListParamFactory();

    public Observable<SortedSet<AdaptationOperation>> diff(final Node before, final Node after) {
        final Observable<SortedSet<AdaptationOperation>> nodes = diffSubnodes(before, after);
        final Observable<SortedSet<AdaptationOperation>> components = diffComponents(before, after);
        final Observable<SortedSet<AdaptationOperation>> dictionary = diffDictionary(before, after);
        final Observable<SortedSet<AdaptationOperation>> group = diffGroup(before, after);
        return Observable.merge(nodes, components, dictionary, group).reduce((strings, strings2) -> {
            strings.addAll(strings2);
            return strings;
        });
    }

    private Observable<SortedSet<AdaptationOperation>> diffGroup(Node before, Node after) {
        final Observable<List<Group>> beforeGroup = observableNodeFactory.getGroupObservable(before).toList();
        final Observable<List<Group>> afterGroup = observableNodeFactory.getGroupObservable(after).toList();
        final Function<Group, AdaptationOperation> trFunction = x -> new RemoveInstance(x.uuid());
        final Function<Group, AdaptationOperation> trFunction1 = y -> new AddInstance(y.uuid());
        final PredicateFactory<Group> tPredicateFactory = a -> b -> a.getName().equals(b.getName()) && typeDefEquals(a, b);
        return getListObservable(beforeGroup, afterGroup, trFunction, trFunction1, tPredicateFactory);
    }

    private Observable<SortedSet<AdaptationOperation>> diffDictionary(Node before, Node after) {
        final Func2<Param, Param, Integer> comparator = (param, param2) -> param.getName().compareTo(param2.getName());
        final Observable<List<Param>> beforeParam = observableNodeFactory.getDictionaryObservable(before).flatMap(observableDictionaryFactory::getParamObservable).toSortedList(comparator);
        final Observable<List<Param>> afterParam = observableNodeFactory.getDictionaryObservable(after).flatMap(observableDictionaryFactory::getParamObservable).toSortedList(comparator);
        return Observable.zip(beforeParam, afterParam, (params, params2) -> {
            final SortedSet<AdaptationOperation> res = new TreeSet<>();
            for (int i = 0; i < Math.min(params.size(), params.size()); i++) {
                final Param prev = params.get(i);
                final Param next = params2.get(i);
                if (!(prev == null || next == null)) {
                    if (prev instanceof BooleanParam && next instanceof BooleanParam) {
                        if (!Objects.equals(((BooleanParam) prev).getValue(), ((BooleanParam) next).getValue())) {
                            res.add(new UpdateInstance(next.uuid()));
                        }
                    } else if (prev instanceof ListParam && next instanceof ListParam) {
                        final Observable<List<Item>> beforeListItem = observableListParamFactory.getValuesObservable((ListParam) prev).toList();
                        final Observable<List<Item>> afterListItem = observableListParamFactory.getValuesObservable((ListParam) next).toList();
                        res.addAll(Observable.zip(beforeListItem, afterListItem, (items, items2) -> {

                            boolean changed;
                            if (items.size() != items2.size()) {
                                changed = true;
                            } else {
                                changed = false;
                                for (int i1 = 0; i1 < items.size(); i1++) {
                                    if (!Objects.equals(items.get(i1).getValue(), items2.get(i1).getValue())) {
                                        changed = true;
                                        break;
                                    }
                                }
                            }
                            final SortedSet<AdaptationOperation> ret = new TreeSet<>();
                            if (changed) {
                                ret.add(new UpdateInstance(next.uuid()));
                            }
                            return ret;
                        }).toBlocking().first());
                    } else if (prev instanceof NumberParam && next instanceof NumberParam) {
                        if (!Objects.equals(((NumberParam) prev).getValue(), ((NumberParam) next).getValue())) {
                            res.add(new UpdateInstance(next.uuid()));
                        }
                    } else if (prev instanceof StringParam && next instanceof StringParam) {
                        if (!Objects.equals(((StringParam) prev).getValue(), ((StringParam) next).getValue())) {
                            res.add(new UpdateInstance(next.uuid()));
                        }
                    }
                }
            }
            return res;
        });
    }

    private Observable<SortedSet<AdaptationOperation>> diffSubnodes(Node before, Node after) {
        final Observable<List<Node>> beforeSubnodes = observableNodeFactory.getSubnodeObservable(before).toList();
        final Observable<List<Node>> afterSubnodes = observableNodeFactory.getSubnodeObservable(after).toList();
        final Function<Node, AdaptationOperation> nodeStringFunction = n -> new RemoveInstance(n.uuid());
        final Function<Node, AdaptationOperation> nodeStringFunction1 = n -> new AddInstance(n.uuid());
        final PredicateFactory<Node> nodePredicateFactory = prevNode -> node -> prevNode.getName().equals(node.getName()) && typeDefEquals(prevNode, node);
        return getListObservable(beforeSubnodes, afterSubnodes, nodeStringFunction, nodeStringFunction1, nodePredicateFactory);
    }

    private Boolean typeDefEquals(Instance prevNode, Instance node) {
        final Observable<TypeDefinition> typeDefObservable1 = observableInstanceFactory.getTypeDefObservable(prevNode);
        final Observable<TypeDefinition> typeDefObservable = observableInstanceFactory.getTypeDefObservable(node);
        return Observable.zip(typeDefObservable, typeDefObservable1, (typeDefinition, typeDefinition2) -> typeDefinition.getName().equals(typeDefinition2.getName()) && typeDefinition.getVersion().equals(typeDefinition2.getVersion())).toBlocking().firstOrDefault(true);
    }

    private <R, T> Observable<SortedSet<R>> getListObservable(Observable<List<T>> beforeSubnodes, Observable<List<T>> afterSubnodes, Function<T, R> func1, Function<T, R> fun2, PredicateFactory<T> compFunc) {
        return beforeSubnodes.zipWith(afterSubnodes, (elem0, elem1) -> {
            return analysis(elem0, elem1,
                    func1,
                    fun2,
                    compFunc).collect(Collectors.toCollection(() -> new TreeSet<>()));
        });
    }

    private Observable<SortedSet<AdaptationOperation>> diffComponents(Node before, Node after) {
        final Observable<List<Component>> beforeComponents = observableNodeFactory.getComponentObservable(before).toList();
        final Observable<List<Component>> afterComponents = observableNodeFactory.getComponentObservable(after).toList();
        final Function<Component, AdaptationOperation> componentStringFunction = n -> new RemoveInstance(n.uuid());
        final Function<Component, AdaptationOperation> componentStringFunction1 = n -> new AddInstance(n.uuid());
        final PredicateFactory<Component> componentPredicateFactory = prevComponent -> newComponent -> prevComponent.getName().equals(newComponent.getName()) && typeDefEquals(prevComponent, newComponent);
        return getListObservable(beforeComponents, afterComponents, componentStringFunction, componentStringFunction1, componentPredicateFactory);
    }

    private <T, U> Stream<U> analysis(List<T> components0, List<T> components1, Function<T, U> opAdd, Function<T, U> opRm, PredicateFactory<T> componentPredicateFactory) {
        final Stream<U> removedNodes = diff(components0, components1, opAdd, componentPredicateFactory);
        final Stream<U> addedNodes = diff(components1, components0, opRm, componentPredicateFactory);
        return concat(removedNodes, addedNodes);
    }

    private <T, U> Stream<U> diff(List<T> nodes0, List<T> nodes1, Function<T, U> nodeStringFunction, PredicateFactory<T> predicateFactory) {
        return nodes0.stream()
                .filter(node -> nodes1.stream()
                        .filter(predicateFactory.get(node)).count() == 0)
                .map(nodeStringFunction);
    }
}
