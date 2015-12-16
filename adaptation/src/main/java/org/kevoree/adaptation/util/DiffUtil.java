package org.kevoree.adaptation.util;

import org.kevoree.*;
import org.kevoree.Dictionary;
import org.kevoree.adaptation.observable.*;
import org.kevoree.adaptation.operation.AddInstance;
import org.kevoree.adaptation.operation.RemoveInstance;
import org.kevoree.adaptation.operation.UpdateInstance;
import org.kevoree.adaptation.operation.util.AdaptationOperation;
import rx.Observable;
import rx.functions.Func1;
import rx.functions.Func2;

import java.util.*;

/**
 * Created by mleduc on 16/12/15.
 */
public class DiffUtil {
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

    private Observable<SortedSet<AdaptationOperation>> diffParams(Observable<Param> paramObservable, Observable<Param> paramObservable1) {
        final Observable<List<Param>> beforeParam = paramObservable.toSortedList(PARAM_COMPARATOR);
        final Observable<List<Param>> afterParam = paramObservable1.toSortedList(PARAM_COMPARATOR);
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

    private Boolean typeDefEquals(Instance prevNode, Instance node) {
        final Observable<TypeDefinition> typeDefObservable1 = observableInstanceFactory.getTypeDefObservable(prevNode);
        final Observable<TypeDefinition> typeDefObservable = observableInstanceFactory.getTypeDefObservable(node);
        return Observable.zip(typeDefObservable, typeDefObservable1, new Func2<TypeDefinition, TypeDefinition, Boolean>() {
            @Override
            public Boolean call(TypeDefinition typeDefinition, TypeDefinition typeDefinition2) {
                return typeDefinition.getName().equals(typeDefinition2.getName()) && typeDefinition.getVersion().equals(typeDefinition2.getVersion());
            }
        }).toBlocking().firstOrDefault(true);
    }

    private <R, T> Observable<SortedSet<R>> getListObservable(final Observable<List<T>> beforeSubnodes, final Observable<List<T>> afterSubnodes, final Function<T, R> func1, final Function<T, R> fun2, final PredicateFactory<T> compFunc) {
        return beforeSubnodes.zipWith(afterSubnodes, new Func2<List<T>, List<T>, SortedSet<R>>() {
            @Override
            public SortedSet<R> call(List<T> elem0, List<T> elem1) {
                final List<R> analysis = DiffUtil.this.analysis(elem0, elem1,
                        func1,
                        fun2,
                        compFunc);
                SortedSet<R> ret = new TreeSet<>();
                ret.addAll(analysis);
                return ret;
            }
        });
    }

    private <T, U> List<U> analysis(List<T> components0, List<T> components1, Function<T, U> opAdd, Function<T, U> opRm, PredicateFactory<T> componentPredicateFactory) {
        final List<U> removedNodes = diff(components0, components1, opAdd, componentPredicateFactory);
        final List<U> addedNodes = diff(components1, components0, opRm, componentPredicateFactory);
        removedNodes.addAll(addedNodes);
        return removedNodes;
    }

    private <T, U> List<U> diff(final List<T> nodes0, final List<T> nodes1, final Function<T, U> nodeStringFunction, final PredicateFactory<T> predicateFactory) {
        final List<U> ret = new ArrayList<>();
        for(T elem: nodes0) {
            boolean found = nodes1.isEmpty();
            for(T elem1: nodes1) {
                if(predicateFactory.get(elem1).test(elem1)) {
                    found = true;
                    break;
                }
            }
            if(found) {
                ret.add(nodeStringFunction.apply(elem));
            }
        }
        return ret;
    }


    public Observable<SortedSet<AdaptationOperation>> diffInput(Channel channel, Channel after) {
        return null;
    }
}
