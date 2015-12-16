package org.kevoree.adaptation;

import org.kevoree.*;
import org.kevoree.adaptation.observable.ObservableDictionaryFactory;
import org.kevoree.adaptation.observable.ObservableInstanceFactory;
import org.kevoree.adaptation.observable.ObservableNodeFactory;
import rx.Observable;
import rx.functions.Func2;

import java.util.ArrayList;
import java.util.List;
import java.util.Objects;
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

    // TODO : remplacer le type string par un vrai typage de moteur d'adaptation.
    public Observable<List<String>> diff(final Node before, final Node after) {
        final Observable<List<String>> nodes = diffSubnodes(before, after);
        final Observable<List<String>> components = diffComponents(before, after);
        final Observable<List<String>> dictionary = diffDictionary(before, after);
        final Observable<List<String>> group = diffGroup(before, after);
        return Observable.merge(nodes, components, dictionary, group).reduce((strings, strings2) -> {
            strings.addAll(strings2);
            return strings;
        });
    }

    private Observable<List<String>> diffGroup(Node before, Node after) {
        final Observable<List<Group>> beforeGroup = observableNodeFactory.getGroupObservable(before).toList();
        final Observable<List<Group>> afterGroup = observableNodeFactory.getGroupObservable(after).toList();
        return getListObservable(beforeGroup, afterGroup, x -> "remove group " + x.getName(), y -> "add group " + y.getName(), a -> b -> a.getName().equals(b.getName()) && typeDefEquals(a, b));
    }

    private Observable<List<String>> diffDictionary(Node before, Node after) {
        final Func2<Param, Param, Integer> comparator = (param, param2) -> param.getName().compareTo(param2.getName());
        final Observable<List<Param>> beforeParam = observableNodeFactory.getDictionaryObservable(before).flatMap(observableDictionaryFactory::getParamObservable).toSortedList(comparator);
        final Observable<List<Param>> afterParam = observableNodeFactory.getDictionaryObservable(after).flatMap(observableDictionaryFactory::getParamObservable).toSortedList(comparator);
        return Observable.zip(beforeParam, afterParam, (params, params2) -> {
            final List<String> res = new ArrayList<>();
            for (int i = 0; i < Math.min(params.size(), params.size()); i++) {
                final Param prev = params.get(i);
                final Param next = params2.get(i);
                if (!(prev == null || next == null)) {
                    if (prev instanceof BooleanParam && next instanceof BooleanParam) {
                        if (!Objects.equals(((BooleanParam) prev).getValue(), ((BooleanParam) next).getValue())) {
                            res.add("update param " + prev.getName());
                        }
                    } else if (prev instanceof ListParam && next instanceof ListParam) {
                        // TODO
                    } else if (prev instanceof NumberParam && next instanceof NumberParam) {
                        if (!Objects.equals(((NumberParam) prev).getValue(), ((NumberParam) next).getValue())) {
                            res.add("update param " + prev.getName());
                        }
                    } else if (prev instanceof StringParam && next instanceof StringParam) {
                        if (!Objects.equals(((StringParam) prev).getValue(), ((StringParam) next).getValue())) {
                            res.add("update param " + prev.getName());
                        }
                    }
                }
            }
            return res;
        });
    }

    private Observable<List<String>> diffSubnodes(Node before, Node after) {
        final Observable<List<Node>> beforeSubnodes = observableNodeFactory.getSubnodeObservable(before).toList();
        final Observable<List<Node>> afterSubnodes = observableNodeFactory.getSubnodeObservable(after).toList();
        final Function<Node, String> nodeStringFunction = n -> "remove subNode " + n.getName();
        final Function<Node, String> nodeStringFunction1 = n -> "add subNode " + n.getName();
        final PredicateFactory<Node> nodePredicateFactory = prevNode -> node -> prevNode.getName().equals(node.getName()) && typeDefEquals(prevNode, node);
        return getListObservable(beforeSubnodes, afterSubnodes, nodeStringFunction, nodeStringFunction1, nodePredicateFactory);
    }

    private Boolean typeDefEquals(Instance prevNode, Instance node) {
        final Observable<TypeDefinition> typeDefObservable1 = observableInstanceFactory.getTypeDefObservable(prevNode);
        final Observable<TypeDefinition> typeDefObservable = observableInstanceFactory.getTypeDefObservable(node);
        return Observable.zip(typeDefObservable, typeDefObservable1, (typeDefinition, typeDefinition2) -> typeDefinition.getName().equals(typeDefinition2.getName()) && typeDefinition.getVersion().equals(typeDefinition2.getVersion())).toBlocking().firstOrDefault(true);
    }

    private <R, T> Observable<List<R>> getListObservable(Observable<List<T>> beforeSubnodes, Observable<List<T>> afterSubnodes, Function<T, R> func1, Function<T, R> fun2, PredicateFactory<T> compFunc) {
        return beforeSubnodes.zipWith(afterSubnodes, (elem0, elem1) -> {
            return analysis(elem0, elem1,
                    func1,
                    fun2,
                    compFunc).collect(Collectors.toList());
        });
    }

    private Observable<List<String>> diffComponents(Node before, Node after) {
        final Observable<List<Component>> beforeComponents = observableNodeFactory.getComponentObservable(before).toList();
        final Observable<List<Component>> afterComponents = observableNodeFactory.getComponentObservable(after).toList();
        final Function<Component, String> componentStringFunction = n -> "remove component " + n.getName();
        final Function<Component, String> componentStringFunction1 = n -> "add component " + n.getName();
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
