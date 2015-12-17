package org.kevoree.adaptation.util;

import org.kevoree.*;
import org.kevoree.Dictionary;
import org.kevoree.adaptation.observable.*;
import org.kevoree.adaptation.observable.ObservablePortFactory;
import org.kevoree.adaptation.operation.AddInstance;
import org.kevoree.adaptation.operation.RemoveInstance;
import org.kevoree.adaptation.operation.UpdateInstance;
import org.kevoree.adaptation.operation.UpdateParam;
import org.kevoree.adaptation.operation.util.AdaptationOperation;
import org.kevoree.adaptation.util.functional.Function;
import org.kevoree.adaptation.util.functional.Predicate;
import org.kevoree.adaptation.util.functional.PredicateFactory;
import rx.Observable;
import rx.functions.Func1;
import rx.functions.Func2;

import java.util.*;

/**
 * Provides the operation needed to compare the changes between to states of a part of the model.
 * Created by mleduc on 16/12/15.
 */
public class DiffUtil {

    /**
     * Compare two params.
     */
    public static final Func2<Param, Param, Integer> PARAM_COMPARATOR = new Func2<Param, Param, Integer>() {
        @Override
        public Integer call(Param param, Param param2) {
            return param.getName().compareTo(param2.getName());
        }
    };

    private final ObservableNodeFactory observableNodeFactory = new ObservableNodeFactory();
    private final ObservableDictionaryFactory observableDictionaryFactory = new ObservableDictionaryFactory();
    private static final ObservableInstanceFactory observableInstanceFactory = new ObservableInstanceFactory();
    private final ObservableListParamFactory observableListParamFactory = new ObservableListParamFactory();
    private final ObservableGroupFactory observableGroupFactory = new ObservableGroupFactory();
    private final ObservableChannelFactory observableChannelFactory = new ObservableChannelFactory();
    private final ObservableComponentFactory observableComponentFactory = new ObservableComponentFactory();
    private final ObservableFragmentDictionaryFactory observableFragmentDictionaryFactory = new ObservableFragmentDictionaryFactory();
    private final ObservablePortFactory observablePortFactory = new ObservablePortFactory();


    public Observable<SortedSet<AdaptationOperation>> diffChannel(Node before, Node after) {
        final Observable<List<Channel>> listObservable = getAllChannelFromNode(before).toList();
        final Observable<List<Channel>> listObservable1 = getAllChannelFromNode(after).toList();
        return getListObservable(listObservable, listObservable1, new Function<Channel, AdaptationOperation>() {
            @Override
            public AdaptationOperation apply(Channel x) {
                return new RemoveInstance(x.uuid());
            }
        }, new Function<Channel, AdaptationOperation>() {
            @Override
            public AdaptationOperation apply(Channel x) {
                return new AddInstance(x.uuid());
            }
        }, new PredicateFactory<Channel>() {
            @Override
            public Predicate<? super Channel> get(final Channel a) {
                return new Predicate<Channel>() {
                    @Override
                    public boolean test(Channel b) {
                        return Objects.equals(a.getName(), b.getName()) && typeDefEquals(a, b);
                    }
                };
            }
        });
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
        final Observable<List<Group>> afterGroup = observableNodeFactory.getGroupObservable(after).toList();
        final Observable<List<Group>> beforeGroup = observableNodeFactory.getGroupObservable(before).toList();
        final Function<Group, AdaptationOperation> trFunction = new Function<Group, AdaptationOperation>() {
            @Override
            public AdaptationOperation apply(Group x) {
                return new RemoveInstance(x.uuid());
            }
        };
        final Function<Group, AdaptationOperation> trFunction1 = new Function<Group, AdaptationOperation>() {
            @Override
            public AdaptationOperation apply(Group y) {
                return new AddInstance(y.uuid());
            }
        };
        final PredicateFactory<Group> tPredicateFactory = new PredicateFactory<Group>() {
            @Override
            public Predicate<? super Group> get(final Group a) {
                return new Predicate<Group>() {
                    @Override
                    public boolean test(Group b) {
                        return a.getName().equals(b.getName()) && typeDefEquals(a, b);
                    }
                };
            }
        };
        return getListObservable(beforeGroup, afterGroup, trFunction, trFunction1, tPredicateFactory);

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
        return diffParams(paramObservable, before.uuid(), paramObservable1, after.uuid());
    }

    /**
     * @param unsortedBeforeParam Previous state of the param.
     * @param beforeParamOwnerId
     * @param unsortedAfterParam  Current state of the param.
     * @param afterParamOwnerId   @return The serie of operations needed to pass from before to after.
     */
    private Observable<SortedSet<AdaptationOperation>> diffParams(Observable<Param> unsortedBeforeParam, long beforeParamOwnerId, Observable<Param> unsortedAfterParam, final long afterParamOwnerId) {
        final Observable<List<Param>> beforeParam = unsortedBeforeParam.toSortedList(PARAM_COMPARATOR);
        final Observable<List<Param>> afterParam = unsortedAfterParam.toSortedList(PARAM_COMPARATOR);
        return Observable.zip(beforeParam, afterParam, new Func2<List<Param>, List<Param>, SortedSet<AdaptationOperation>>() {
            @Override
            public SortedSet<AdaptationOperation> call(List<Param> params, List<Param> params2) {
                final SortedSet<AdaptationOperation> res = new TreeSet<>();
                for (int i = 0; i < Math.min(params.size(), params2.size()); i++) {
                    final Param prev = params.get(i);
                    final Param next = params2.get(i);
                    if (!(prev == null || next == null)) {
                        if (prev instanceof BooleanParam && next instanceof BooleanParam) {
                            if (!Objects.equals(((BooleanParam) prev).getValue(), ((BooleanParam) next).getValue())) {
                                res.add(new UpdateParam(next.uuid()));
                                res.add(new UpdateInstance(afterParamOwnerId));
                            }
                        } else if (prev instanceof ListParam && next instanceof ListParam) {
                            final Observable<List<Item>> beforeListItem = observableListParamFactory.getValuesObservable((ListParam) prev).toList();
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
                                        ret.add(new UpdateParam(next.uuid()));
                                        ret.add(new UpdateInstance(afterParamOwnerId));
                                    }
                                    return ret;
                                }
                            }).toBlocking().first());
                        } else if (prev instanceof NumberParam && next instanceof NumberParam) {
                            if (!Objects.equals(((NumberParam) prev).getValue(), ((NumberParam) next).getValue())) {
                                res.add(new UpdateParam(next.uuid()));
                                res.add(new UpdateInstance(afterParamOwnerId));
                            }
                        } else if (prev instanceof StringParam && next instanceof StringParam) {
                            if (!Objects.equals(((StringParam) prev).getValue(), ((StringParam) next).getValue())) {
                                res.add(new UpdateParam(next.uuid()));
                                res.add(new UpdateInstance(afterParamOwnerId));
                            }
                        }
                    }
                }
                return res;
            }
        });
    }

    /**
     * This method target only the output ports of the channel.
     *
     * @param before Previous state of the channel.
     * @param after  Current state of the channel.
     * @return The serie of operations needed to pass from before to after.
     */
    public Observable<SortedSet<AdaptationOperation>> diffOutput(Channel before, Channel after) {
        final Observable<List<Port>> beforeInputPorts = this.observableChannelFactory.getOutputObservable(before).map(new Func1<OutputPort, Port>() {
            @Override
            public Port call(OutputPort outputPort) {
                return outputPort;
            }
        }).toList();
        final Observable<List<Port>> afterInputPorts = this.observableChannelFactory.getOutputObservable(after).map(new Func1<OutputPort, Port>() {
            @Override
            public Port call(OutputPort outputPort) {
                return outputPort;
            }
        }).toList();
        return getListObservable(beforeInputPorts, afterInputPorts, new Function<Port, AdaptationOperation>() {
            @Override
            public AdaptationOperation apply(Port x) {
                return new RemoveInstance(x.uuid());
            }
        }, new Function<Port, AdaptationOperation>() {
            @Override
            public AdaptationOperation apply(Port x) {
                return new AddInstance(x.uuid());
            }
        }, new PortComparator(observablePortFactory, observableComponentFactory));
    }

    /**
     * This method target only the input ports of the channel.
     *
     * @param before The previous state of the channel.
     * @param after  The current state of the channel.
     * @return The serie of operations needed to pass from before to after.
     */
    public Observable<SortedSet<AdaptationOperation>> diffInput(Channel before, Channel after) {
        final Observable<List<Port>> beforeInputPorts = this.observableChannelFactory.getInputObservable(before).map(new Func1<InputPort, Port>() {
            @Override
            public Port call(InputPort inputPort) {
                return inputPort;
            }
        }).toList();
        final Observable<List<Port>> afterInputPorts = this.observableChannelFactory.getInputObservable(after).map(new Func1<InputPort, Port>() {
            @Override
            public Port call(InputPort inputPort) {
                return inputPort;
            }
        }).toList();

        return getListObservable(beforeInputPorts, afterInputPorts, new Function<Port, AdaptationOperation>() {
            @Override
            public AdaptationOperation apply(Port x) {
                return new RemoveInstance(x.uuid());
            }
        }, new Function<Port, AdaptationOperation>() {
            @Override
            public AdaptationOperation apply(Port x) {
                return new AddInstance(x.uuid());
            }
        }, new PortComparator(observablePortFactory, observableComponentFactory));
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
        final Function<Component, AdaptationOperation> componentStringFunction = new Function<Component, AdaptationOperation>() {
            @Override
            public AdaptationOperation apply(Component n) {
                return new RemoveInstance(n.uuid());
            }
        };
        final Function<Component, AdaptationOperation> componentStringFunction1 = new Function<Component, AdaptationOperation>() {
            @Override
            public AdaptationOperation apply(Component n) {
                return new AddInstance(n.uuid());
            }
        };
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
        return getListObservable(beforeComponents, afterComponents, componentStringFunction, componentStringFunction1, componentPredicateFactory);
    }

    private static boolean componentEquality(Component newComponent, Component prevComponent) {
        final boolean nameEquals = prevComponent.getName().equals(newComponent.getName());
        final Boolean sameTypeDef = typeDefEquals(prevComponent, newComponent);
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
        final Function<Node, AdaptationOperation> nodeStringFunction = new Function<Node, AdaptationOperation>() {
            @Override
            public AdaptationOperation apply(Node n) {
                return new RemoveInstance(n.uuid());
            }
        };
        final Function<Node, AdaptationOperation> nodeStringFunction1 = new Function<Node, AdaptationOperation>() {
            @Override
            public AdaptationOperation apply(Node n) {
                return new AddInstance(n.uuid());
            }
        };
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
        return getListObservable(beforeSubnodes, afterSubnodes, nodeStringFunction, nodeStringFunction1, nodePredicateFactory);
    }

    private static boolean nodeEquality(Node prev, Node current) {
        final String prevName = prev.getName();
        final String currentName = current.getName();
        final boolean nameEquality = Objects.equals(prevName, currentName);
        return nameEquality && typeDefEquals(prev, current);
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

        return diffParams(beforeParams, before.uuid(), afterParams, after.uuid());
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

        return diffParams(beforeParams, before.uuid(), afterParams, after.uuid());
    }

    /**
     * @param previous The previous instance
     * @param next     The current instance
     * @return True if previous and next shares the same type definition.
     */
    private static Boolean typeDefEquals(Instance previous, Instance next) {
        final Observable<TypeDefinition> previousTypeDefinition = observableInstanceFactory.getTypeDefObservable(previous);
        final Observable<TypeDefinition> nextTypeDefinition = observableInstanceFactory.getTypeDefObservable(next);
        return Observable.zip(nextTypeDefinition, previousTypeDefinition, new Func2<TypeDefinition, TypeDefinition, Boolean>() {
            @Override
            public Boolean call(TypeDefinition typeDef0, TypeDefinition typeDef1) {
                return typeDef0.getName().equals(typeDef1.getName()) && typeDef0.getVersion().equals(typeDef1.getVersion());
            }
        }).toBlocking().firstOrDefault(true);
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
    private <R, T> Observable<SortedSet<R>> getListObservable(final Observable<List<T>> before, final Observable<List<T>> after, final Function<T, R> removedElementOperation, final Function<T, R> addElementOperation, final PredicateFactory<T> comparingElements) {
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
}
