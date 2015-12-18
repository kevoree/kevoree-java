package org.kevoree.adaptation.util;

import com.github.zafarkhaja.semver.Version;
import org.kevoree.*;
import org.kevoree.Dictionary;
import org.kevoree.adaptation.observable.*;
import org.kevoree.adaptation.operation.*;
import org.kevoree.adaptation.operation.util.AdaptationOperation;
import org.kevoree.adaptation.util.comparators.ParamComparator;
import org.kevoree.adaptation.util.comparators.TypeDefEquality;
import org.kevoree.adaptation.util.functional.Function;
import org.kevoree.adaptation.util.functional.Predicate;
import org.kevoree.adaptation.util.functional.PredicateFactory;
import org.kevoree.adaptation.util.predicates.ChannelPredicateFactory;
import rx.Observable;
import rx.functions.Func1;
import rx.functions.Func2;

import java.util.*;

/**
 * Provides the operation needed to compare the changes between to states of a part of the model.
 * Created by mleduc on 16/12/15.
 */
public class DiffUtil {

    private final ObservableNodeFactory observableNodeFactory = new ObservableNodeFactory();
    private final ObservableDictionaryFactory observableDictionaryFactory = new ObservableDictionaryFactory();
    private static final ObservableInstanceFactory observableInstanceFactory = new ObservableInstanceFactory();
    private final ObservableListParamFactory observableListParamFactory = new ObservableListParamFactory();
    private final ObservableGroupFactory observableGroupFactory = new ObservableGroupFactory();
    private final ObservableChannelFactory observableChannelFactory = new ObservableChannelFactory();
    private final ObservableComponentFactory observableComponentFactory = new ObservableComponentFactory();
    private final ObservableFragmentDictionaryFactory observableFragmentDictionaryFactory = new ObservableFragmentDictionaryFactory();
    private final ObservablePortFactory observablePortFactory = new ObservablePortFactory();
    private final ObservableTypeDefinitionFactory observableTypeDefinitionFactory = new ObservableTypeDefinitionFactory();
    private static final TypeDefEquality typeDefEquality = new TypeDefEquality();


    /**
     * The deploy unit used by a component is the latest of its typedef, sorted by semver of its version for the platform of the node.
     *
     * @param before Previous state of the node.
     * @param after  Current state of the node.
     * @param platform
     * @return The list of operation needed to adapt the deploy units.
     */
    public Observable<SortedSet<AdaptationOperation>> diffDeployUnit(Node before, Node after, String platform) {
        final Observable<List<DeployUnit>> listTypeDefBefore = loadTypeDefNode(before, platform);
        final Observable<List<DeployUnit>> listTypeDefAfter = loadTypeDefNode(after, platform);
        return searchAdaptations(listTypeDefBefore, listTypeDefAfter, new RemoveDeployUnitOperation(), new AddDeployUnitOperation(), new DeployUnitPredicateFactory());
    }

    private Observable<List<DeployUnit>> loadTypeDefNode(final Node before, final String platform) {
        /* elements with type def =
            - channels
          - subNodes
          - group
          - components

          */
        final Observable<Channel> listObservable = getAllChannelFromNode(before);
        final Observable<Node> beforeSubnodes = observableNodeFactory.getSubnodeObservable(before);
        final Observable<Group> afterGroup = observableNodeFactory.getGroupObservable(before);
        final Observable<Component> beforeComponents = observableNodeFactory.getComponentObservable(before);
        return Observable.merge(listObservable, beforeSubnodes, afterGroup, beforeComponents).flatMap(new Func1<Instance, Observable<TypeDefinition>>() {
            @Override
            public Observable<TypeDefinition> call(Instance instances) {
                return observableInstanceFactory.getTypeDefObservable(instances);
            }
        }).map(new Func1<TypeDefinition, DeployUnit>() {
            @Override
            public DeployUnit call(TypeDefinition typeDefinition) {
                final List<DeployUnit> first = observableTypeDefinitionFactory.getDeployUnitObservable(typeDefinition).toList().toBlocking().first();

                // first we select the typeDef related to the node's platform.
                final List<DeployUnit> plat = filteringByPlatform(first);

                // then we sort the according to their semver.
                Collections.sort(plat, new Comparator<DeployUnit>() {
                    @Override
                    public int compare(DeployUnit o1, DeployUnit o2) {
                        final Version v1 = Version.valueOf(o1.getVersion());
                        final Version v2 = Version.valueOf(o2.getVersion());
                        return v1.compareTo(v2);
                    }
                });

                // and we select the latest.
                Collections.reverse(plat);
                final DeployUnit ret;
                if(plat.isEmpty()) {
                    ret = null;
                } else {
                    ret = plat.get(0);
                }
                return ret;
            }

            private List<DeployUnit> filteringByPlatform(List<DeployUnit> first) {
                final List<DeployUnit> plat = new ArrayList<>();
                for(DeployUnit du: first) {
                    if(Objects.equals(platform, du.getPlatform())) {
                       plat.add(du);
                    }
                }
                return plat;
            }
        }).filter(new Func1<DeployUnit, Boolean>() {
            @Override
            public Boolean call(DeployUnit deployUnit) {
                return deployUnit != null;
            }
        }).toList();
    }

    public Observable<SortedSet<AdaptationOperation>> diffChannel(Node before, Node after) {
        final Observable<List<Channel>> listObservable = getAllChannelFromNode(before).toList();
        final Observable<List<Channel>> listObservable1 = getAllChannelFromNode(after).toList();
        return searchAdaptations(listObservable, listObservable1, new RemoveInstanceOperation<Channel>(), new AddInstanceOperation<Channel>(), new ChannelPredicateFactory());
    }

    private Observable<Channel> getAllChannelFromNode(Node before) {
        return observableNodeFactory.getComponentObservable(before).flatMap(new Func1<Component, Observable<Port>>() {
            @Override
            public Observable<Port> call(Component component) {
                return Observable.merge(observableComponentFactory.getInputPortFactory(component), observableComponentFactory.getOutputPortFactory(component));
            }
        }).flatMap(new Func1<Port, Observable<Channel>>() {
            @Override
            public Observable<Channel> call(Port port) {
                return observablePortFactory.getChannelObservable(port);
            }
        });
    }

    /**
     * This method target only the groups of the node.
     *
     * @param before Previous state of the node.
     * @param after  Current state of the node.
     * @return The serie of operations needed to pass from before to after.
     */
    public Observable<SortedSet<AdaptationOperation>> diffGroup(Node before, Node after) {
        final Observable<List<Group>> beforeGroup = observableNodeFactory.getGroupObservable(before).toList();
        final Observable<List<Group>> afterGroup = observableNodeFactory.getGroupObservable(after).toList();
        return searchAdaptations(beforeGroup, afterGroup, new RemoveInstanceOperation<Group>(), new AddInstanceOperation<Group>(), new GroupPredicateFactory());

    }

    /**
     * This method target only the params of an instance.
     *
     * @param before Previous state of the instance.
     * @param after  Current state of the instance.
     * @return The serie of operations needed to pass from before to after.
     */
    public Observable<SortedSet<AdaptationOperation>> diffDictionary(Instance before, Instance after) {
        final Observable<Param> paramObservable = observableInstanceFactory.getDictionaryObservable(before).flatMap(new Func1<Dictionary, Observable<? extends Param>>() {
            @Override
            public Observable<? extends Param> call(Dictionary x) {
                return observableDictionaryFactory.getParamObservable(x);
            }
        });
        final Observable<Param> paramObservable1 = observableInstanceFactory.getDictionaryObservable(after).flatMap(new Func1<Dictionary, Observable<? extends Param>>() {
            @Override
            public Observable<? extends Param> call(Dictionary x) {
                return observableDictionaryFactory.getParamObservable(x);
            }
        });
        return diffParams(paramObservable, paramObservable1, after);
    }

    /**
     * @param unsortedBeforeParam Previous state of the param.
     * @param unsortedAfterParam  Current state of the param.
     * @param afterParamOwner     @return The serie of operations needed to pass from before to after.
     */
    private Observable<SortedSet<AdaptationOperation>> diffParams(final Observable<Param> unsortedBeforeParam, final Observable<Param> unsortedAfterParam, final Instance afterParamOwner) {
        final Observable<List<Param>> beforeParam = unsortedBeforeParam.toSortedList(new ParamComparator());
        final Observable<List<Param>> afterParam = unsortedAfterParam.toSortedList(new ParamComparator());
        return Observable.zip(beforeParam, afterParam, new Func2<List<Param>, List<Param>, SortedSet<AdaptationOperation>>() {
            @Override
            public SortedSet<AdaptationOperation> call(List<Param> params, List<Param> params2) {
                final SortedSet<AdaptationOperation> res = new TreeSet<>();
                for (int i = 0; i < Math.min(params.size(), params2.size()); i++) {
                    compareParams(res, params.get(i), params2.get(i), afterParamOwner);
                }
                return res;
            }
        });
    }

    private void compareParams(final SortedSet<AdaptationOperation> res, final Param prev, final Param next, final Instance afterParamOwnerId) {
        if (!(prev == null || next == null)) {
            if (prev instanceof BooleanParam && next instanceof BooleanParam) {
                compareParamsBoolean(res, (BooleanParam) prev, next, afterParamOwnerId);
            } else if (prev instanceof ListParam && next instanceof ListParam) {
                compareParamsList(res, (ListParam) prev, next, afterParamOwnerId);
            } else if (prev instanceof NumberParam && next instanceof NumberParam) {
                compareParams(res, next, afterParamOwnerId, ((NumberParam) prev).getValue(), ((NumberParam) next).getValue());
            } else if (prev instanceof StringParam && next instanceof StringParam) {
                compareParams(res, next, afterParamOwnerId, ((StringParam) prev).getValue(), ((StringParam) next).getValue());
            }
        }
    }

    private void compareParams(SortedSet<AdaptationOperation> res, Param next, Instance afterParam, String value, String value2) {
        if (!Objects.equals(value, value2)) {
            res.add(new UpdateParam(next));
            res.add(new UpdateInstance(afterParam));
        }
    }

    private void compareParamsList(SortedSet<AdaptationOperation> res, ListParam prev, final Param next, final Instance afterParamOwner) {
        final Observable<List<Item>> beforeListItem = observableListParamFactory.getValuesObservable(prev).toList();
        final Observable<List<Item>> afterListItem = observableListParamFactory.getValuesObservable((ListParam) next).toList();
        res.addAll(Observable.zip(beforeListItem, afterListItem, new Func2<List<Item>, List<Item>, SortedSet<AdaptationOperation>>() {
            @Override
            public SortedSet<AdaptationOperation> call(List<Item> items, List<Item> items2) {

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
                    ret.add(new UpdateParam(next));
                    ret.add(new UpdateInstance(afterParamOwner));
                }
                return ret;
            }
        }).toBlocking().first());
    }

    private void compareParamsBoolean(SortedSet<AdaptationOperation> res, BooleanParam prev, Param next, Instance afterParamOwnerId) {
        if (!Objects.equals(prev.getValue(), ((BooleanParam) next).getValue())) {
            res.add(new UpdateParam(next));
            res.add(new UpdateInstance(afterParamOwnerId));
        }
    }

    /**
     * This method target only the output ports of the channel.
     *
     * @param before Previous state of the channel.
     * @param after  Current state of the channel.
     * @return The serie of operations needed to pass from before to after.
     */
    public Observable<SortedSet<AdaptationOperation>> diffOutput(Channel before, Channel after) {
        final Observable<List<Port>> beforeInputPorts = this.observableChannelFactory.getOutputObservable(before).map(new Upcast<Port>()).toList();
        final Observable<List<Port>> afterInputPorts = this.observableChannelFactory.getOutputObservable(after).map(new Upcast<Port>()).toList();
        return searchAdaptations(beforeInputPorts, afterInputPorts, new RemoveBindingOperation(), new AddBindingOperation(), new PortComparator(observablePortFactory, observableComponentFactory));
    }

    /**
     * This method target only the input ports of the channel.
     *
     * @param before The previous state of the channel.
     * @param after  The current state of the channel.
     * @return The serie of operations needed to pass from before to after.
     */
    public Observable<SortedSet<AdaptationOperation>> diffInput(Channel before, Channel after) {
        final Observable<List<Port>> beforeInputPorts = this.observableChannelFactory.getInputObservable(before).map(new Upcast<Port>()).toList();
        final Observable<List<Port>> afterInputPorts = this.observableChannelFactory.getInputObservable(after).map(new Upcast<Port>()).toList();
        return searchAdaptations(beforeInputPorts, afterInputPorts, new RemoveBindingOperation(), new AddBindingOperation(), new PortComparator(observablePortFactory, observableComponentFactory));
    }

    /**
     * This method target only the component of the node.
     *
     * @param before The previous state of the node.
     * @param after  The current state of the node.
     * @return The serie of operations needed to pass from before to after.
     */
    public Observable<SortedSet<AdaptationOperation>> diffComponents(Node before, Node after) {
        final Observable<List<Component>> beforeComponents = observableNodeFactory.getComponentObservable(before).toList();
        final Observable<List<Component>> afterComponents = observableNodeFactory.getComponentObservable(after).toList();
        final PredicateFactory<Component> componentPredicateFactory = new PredicateFactory<Component>() {
            @Override
            public Predicate<? super Component> get(final Component prevComponent) {
                return new Predicate<Component>() {
                    @Override
                    public boolean test(Component newComponent) {
                        return componentEquality(newComponent, prevComponent);
                    }
                };
            }
        };
        return searchAdaptations(beforeComponents, afterComponents, new RemoveInstanceOperation<Component>(), new AddInstanceOperation<Component>(), componentPredicateFactory);
    }

    private static boolean componentEquality(Component newComponent, Component prevComponent) {
        final boolean nameEquals = Objects.equals(prevComponent.getName(), newComponent.getName());
        final Boolean sameTypeDef = typeDefEquality.typeDefEquals(prevComponent, newComponent);
        return nameEquals && sameTypeDef;
    }

    /**
     * This method target only the subNode of the node.
     *
     * @param before The previous state of the node.
     * @param after  The current state of the node.
     * @return The serie of operations needed to pass from before to after.
     */
    public Observable<SortedSet<AdaptationOperation>> diffSubnodes(Node before, Node after) {
        final Observable<List<Node>> beforeSubnodes = observableNodeFactory.getSubnodeObservable(before).toList();
        final Observable<List<Node>> afterSubnodes = observableNodeFactory.getSubnodeObservable(after).toList();
        final PredicateFactory<Node> nodePredicateFactory = new PredicateFactory<Node>() {
            @Override
            public Predicate<? super Node> get(final Node prevNode) {
                return new Predicate<Node>() {
                    @Override
                    public boolean test(Node node) {
                        return nodeEquality(prevNode, node);
                    }
                };
            }
        };
        return searchAdaptations(beforeSubnodes, afterSubnodes, new RemoveInstanceOperation<Node>(), new AddInstanceOperation<Node>(), nodePredicateFactory);
    }

    private static boolean nodeEquality(Node prev, Node current) {
        final String prevName = prev.getName();
        final String currentName = current.getName();
        final boolean nameEquality = Objects.equals(prevName, currentName);
        return nameEquality && typeDefEquality.typeDefEquals(prev, current);
    }

    /**
     * This method target only the Params of the group.
     *
     * @param before The previous state of the group.
     * @param after  The current state of the group.
     * @return The serie of operations needed to pass from before to after.
     */
    public Observable<SortedSet<AdaptationOperation>> diffFragmentDictionary(Group before, Group after) {
        final Observable<Param> beforeParams = observableGroupFactory.getFragmentDictionaryObservable(before).flatMap(new Func1<FragmentDictionary, Observable<Param>>() {
            @Override
            public Observable<Param> call(FragmentDictionary fragmentDictionaries) {
                return observableFragmentDictionaryFactory.getParamObservable(fragmentDictionaries);
            }
        });
        final Observable<Param> afterParams = observableGroupFactory.getFragmentDictionaryObservable(after).flatMap(new Func1<FragmentDictionary, Observable<Param>>() {
            @Override
            public Observable<Param> call(FragmentDictionary fragmentDictionaries) {
                return observableFragmentDictionaryFactory.getParamObservable(fragmentDictionaries);
            }
        });

        return diffParams(beforeParams, afterParams, after);
    }

    /**
     * This method targets only the fragment dictionary parameters of the channel.
     *
     * @param before The previous state of the channel.
     * @param after  The current state of the channel.
     * @return The serie of operations needed to pass from before to after.
     */
    public Observable<SortedSet<AdaptationOperation>> diffFragmentDictionary(Channel before, Channel after) {
        final Observable<Param> beforeParams = observableChannelFactory.getFragmentDictionaryObservable(before).flatMap(new Func1<FragmentDictionary, Observable<Param>>() {
            @Override
            public Observable<Param> call(FragmentDictionary fragmentDictionaries) {
                return observableFragmentDictionaryFactory.getParamObservable(fragmentDictionaries);
            }
        });
        final Observable<Param> afterParams = observableChannelFactory.getFragmentDictionaryObservable(after).flatMap(new Func1<FragmentDictionary, Observable<Param>>() {
            @Override
            public Observable<Param> call(FragmentDictionary fragmentDictionaries) {
                return observableFragmentDictionaryFactory.getParamObservable(fragmentDictionaries);
            }
        });

        return diffParams(beforeParams, afterParams, after);
    }


    /**
     * @param before                  A list of element in the previous state.
     * @param after                   A list of element in the current state.
     * @param removedElementOperation Function executed when an element is removed between before and after.
     * @param addElementOperation     Function executed when an element id added between before and after.
     * @param comparingElements       The function executed to determine the equality of two elements of before and after.
     * @param <R>                     The type of the element returned post processing
     * @param <T>                     The type of the elements contained in before and after.
     * @return A list of elements.
     */
    private <R, T> Observable<SortedSet<R>> searchAdaptations(final Observable<List<T>> before, final Observable<List<T>> after, final Function<T, R> removedElementOperation, final Function<T, R> addElementOperation, final PredicateFactory<T> comparingElements) {
        return before.zipWith(after, new Func2<List<T>, List<T>, SortedSet<R>>() {
            @Override
            public SortedSet<R> call(List<T> elem0, List<T> elem1) {
                final List<R> analysis = DiffUtil.this.analysis(elem0, elem1,
                        removedElementOperation,
                        addElementOperation,
                        comparingElements);
                SortedSet<R> ret = new TreeSet<>();
                ret.addAll(analysis);
                return ret;
            }
        });
    }

    /**
     * @param before            A list of element in the previous state
     * @param after             A list of element in the current state
     * @param opRemoved         Operation executed on removed elements
     * @param opAdded           Operation executed on added elements
     * @param comparingElements Operation executed to compare if the elements are equals
     * @param <T>               The type returned by opRemoved/opAdded
     * @param <U>               The type of the elements in before/after
     * @return A list of elements.
     */
    private <T, U> List<U> analysis(List<T> before, List<T> after, Function<T, U> opRemoved, Function<T, U> opAdded, PredicateFactory<T> comparingElements) {
        final List<U> removedNodes = diff(before, after, opRemoved, comparingElements);
        final List<U> addedNodes = diff(after, before, opAdded, comparingElements);
        removedNodes.addAll(addedNodes);
        return removedNodes;
    }

    /**
     * Diff will look for element of list0 into list1. if not found the operation map will be executed on each not found elements and returned in the form of a list of type U.
     *
     * @param list0    First list
     * @param list1    Second list
     * @param map      A T to U operation.
     * @param equality Determine of two element of type T are equals.
     * @param <T>      The type of the elements int list0 and list1
     * @param <U>      The type returned by map/
     * @return A list of type U
     */
    private <T, U> List<U> diff(final List<T> list0, final List<T> list1, final Function<T, U> map, final PredicateFactory<T> equality) {
        final List<U> ret = new ArrayList<>();
        for (T elem : list0) {
            boolean found = false;
            final Predicate<? super T> predicate = equality.get(elem);
            for (T elem1 : list1) {
                if (predicate.test(elem1)) {
                    found = true;
                    break;
                }
            }
            if (!found) {
                ret.add(map.apply(elem));
            }
        }
        return ret;
    }

    public Observable<SortedSet<AdaptationOperation>> diffInstanceStatus(Instance before, Instance after) {
        final SortedSet<AdaptationOperation>[] res;
        if ((Objects.equals(before.getStarted(), false) || before.getStarted() == null) && Objects.equals(after.getStarted(), true)) {
            res = new SortedSet[1];
            res[0] = new TreeSet<>();
            res[0].add(new StartInstance(after));
        } else if (Objects.equals(before.getStarted(), true) && Objects.equals(after.getStarted(), false)) {
            res = new SortedSet[1];
            res[0] = new TreeSet<>();
            res[0].add(new StopInstance(after));
        } else {
            res = new SortedSet[0];
        }

        return Observable.from(res);
    }


    private static class PortComparator implements PredicateFactory<Port> {

        private final ObservableComponentFactory observableComponentFactory;
        public ObservablePortFactory observablePortFactory;

        public PortComparator(ObservablePortFactory observablePortFactory, ObservableComponentFactory observableComponentFactory) {
            this.observablePortFactory = observablePortFactory;
            this.observableComponentFactory = observableComponentFactory;
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
                                if (componentEquality(component, component2)) {
                                    final Observable<Node> hostA = observableComponentFactory.getHostObservable(component);
                                    final Observable<Node> hostB = observableComponentFactory.getHostObservable(component2);
                                    final Boolean res = Observable.zip(hostA, hostB, new Func2<Node, Node, Boolean>() {
                                        @Override
                                        public Boolean call(Node node, Node node2) {
                                            final boolean res = nodeEquality(node, node2);
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

    private static class RemoveInstanceOperation<T extends Instance> implements Function<T, AdaptationOperation> {
        @Override
        public AdaptationOperation apply(T o) {
            return new RemoveInstance(o);
        }
    }

    private static class AddInstanceOperation<T extends Instance> implements Function<T, AdaptationOperation> {
        @Override
        public AdaptationOperation apply(T o) {
            return new AddInstance(o);
        }
    }

    private static class RemoveBindingOperation<T extends Port> implements Function<T, AdaptationOperation> {
        @Override
        public AdaptationOperation apply(T o) {
            return new RemoveBinding(o);
        }
    }

    private static class AddBindingOperation<T extends Port> implements Function<T, AdaptationOperation> {
        @Override
        public AdaptationOperation apply(T o) {
            return new AddBinding(o);
        }
    }

    private static class GroupPredicateFactory implements PredicateFactory<Group> {
        @Override
        public Predicate<? super Group> get(final Group a) {
            return new Predicate<Group>() {
                @Override
                public boolean test(Group b) {
                    return a.getName().equals(b.getName()) && typeDefEquality.typeDefEquals(a, b);
                }
            };
        }
    }

    private static class Upcast<T> implements Func1<T, T> {
        @Override
        public T call(T inputPort) {
            return inputPort;
        }
    }

    private class RemoveDeployUnitOperation implements Function<DeployUnit, AdaptationOperation> {
        @Override
        public AdaptationOperation apply(DeployUnit du) {
            return new RemoveDeployUnit(du);
        }
    }

    private class AddDeployUnitOperation implements Function<DeployUnit, AdaptationOperation> {
        @Override
        public AdaptationOperation apply(DeployUnit du) {
            return new AddDeployUnit(du);
        }
    }

    private class DeployUnitPredicateFactory implements PredicateFactory<DeployUnit> {
        @Override
        public Predicate<? super DeployUnit> get(final DeployUnit a) {
            return new Predicate<DeployUnit>() {
                @Override
                public boolean test(DeployUnit b) {
                    return Objects.equals(a.getName(), b.getName()) && Objects.equals(a.getVersion(), b.getVersion()) && Objects.equals(a.getPlatform(), b.getPlatform());
                }
            };
        }
    }
}
