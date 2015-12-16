package org.kevoree.adaptation.util;

import org.kevoree.*;
import org.kevoree.Dictionary;
import org.kevoree.adaptation.observable.*;
import org.kevoree.adaptation.operation.AddInstance;
import org.kevoree.adaptation.operation.RemoveInstance;
import org.kevoree.adaptation.operation.UpdateInstance;
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
    private final ObservableInstanceFactory observableInstanceFactory = new ObservableInstanceFactory();
    private final ObservableListParamFactory observableListParamFactory = new ObservableListParamFactory();
    private final ObservableGroupFactory observableGroupFactory = new ObservableGroupFactory();
    private final ObservableChannelFactory observableChannelFactory = new ObservableChannelFactory();
    private final ObservableFragmentDictionaryFactory observableFragmentDictionaryFactory = new ObservableFragmentDictionaryFactory();


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
        return diffParams(paramObservable, paramObservable1);
    }

    /**
     * @param unsortedBeforeParam Previous state of the param.
     * @param unsortedAfterParam  Current state of the param.
     * @return The serie of operations needed to pass from before to after.
     */
    private Observable<SortedSet<AdaptationOperation>> diffParams(Observable<Param> unsortedBeforeParam, Observable<Param> unsortedAfterParam) {
        final Observable<List<Param>> beforeParam = unsortedBeforeParam.toSortedList(PARAM_COMPARATOR);
        final Observable<List<Param>> afterParam = unsortedAfterParam.toSortedList(PARAM_COMPARATOR);
        return Observable.zip(beforeParam, afterParam, new Func2<List<Param>, List<Param>, SortedSet<AdaptationOperation>>() {
            @Override
            public SortedSet<AdaptationOperation> call(List<Param> params, List<Param> params2) {
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
                                        ret.add(new UpdateInstance(next.uuid()));
                                    }
                                    return ret;
                                }
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
        final Observable<List<OutputPort>> beforeInputPorts = this.observableChannelFactory.getOutputObservable(before).toList();
        final Observable<List<OutputPort>> afterInputPorts = this.observableChannelFactory.getOutputObservable(after).toList();
        return getListObservable(beforeInputPorts, afterInputPorts, new Function<OutputPort, AdaptationOperation>() {
            @Override
            public AdaptationOperation apply(OutputPort x) {
                return new RemoveInstance(x.uuid());
            }
        }, new Function<OutputPort, AdaptationOperation>() {
            @Override
            public AdaptationOperation apply(OutputPort x) {
                return new AddInstance(x.uuid());
            }
        }, new PredicateFactory<OutputPort>() {
            @Override
            public Predicate<? super OutputPort> get(final OutputPort a) {
                return new Predicate<OutputPort>() {
                    @Override
                    public boolean test(OutputPort b) {
                        return Objects.equals(a.getName(), b.getName());
                    }
                };
            }
        });
    }

    /**
     * This method target only the input ports of the channel.
     *
     * @param before The previous state of the channel.
     * @param after  The current state of the channel.
     * @return The serie of operations needed to pass from before to after.
     */
    public Observable<SortedSet<AdaptationOperation>> diffInput(Channel before, Channel after) {
        final Observable<List<InputPort>> beforeInputPorts = this.observableChannelFactory.getInputObservable(before).toList();
        final Observable<List<InputPort>> afterInputPorts = this.observableChannelFactory.getInputObservable(after).toList();

        return getListObservable(beforeInputPorts, afterInputPorts, new Function<InputPort, AdaptationOperation>() {
            @Override
            public AdaptationOperation apply(InputPort x) {
                return new RemoveInstance(x.uuid());
            }
        }, new Function<InputPort, AdaptationOperation>() {
            @Override
            public AdaptationOperation apply(InputPort x) {
                return new AddInstance(x.uuid());
            }
        }, new PredicateFactory<InputPort>() {
            @Override
            public Predicate<? super InputPort> get(final InputPort a) {
                return new Predicate<InputPort>() {
                    @Override
                    public boolean test(InputPort b) {
                        return Objects.equals(a.getName(), b.getName());
                    }
                };
            }
        });
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
                        return prevComponent.getName().equals(newComponent.getName()) && DiffUtil.this.typeDefEquals(prevComponent, newComponent);
                    }
                };
            }
        };
        return getListObservable(beforeComponents, afterComponents, componentStringFunction, componentStringFunction1, componentPredicateFactory);
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
                        return prevNode.getName().equals(node.getName()) && DiffUtil.this.typeDefEquals(prevNode, node);
                    }
                };
            }
        };
        return getListObservable(beforeSubnodes, afterSubnodes, nodeStringFunction, nodeStringFunction1, nodePredicateFactory);
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

        return diffParams(beforeParams, afterParams);
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

        return diffParams(beforeParams, afterParams);
    }

    /**
     * @param previous The previous instance
     * @param next     The current instance
     * @return True if previous and next shares the same type definition.
     */
    private Boolean typeDefEquals(Instance previous, Instance next) {
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
            boolean found = list1.isEmpty();
            for (T elem1 : list1) {
                if (equality.get(elem1).test(elem1)) {
                    found = true;
                    break;
                }
            }
            if (found) {
                ret.add(map.apply(elem));
            }
        }
        return ret;
    }


}
