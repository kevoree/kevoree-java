package org.kevoree.adaptation.util.comparators;

import org.kevoree.Instance;
import org.kevoree.TypeDefinition;
import org.kevoree.adaptation.observable.ObservableInstanceFactory;
import rx.Observable;
import rx.functions.Func2;

/**
 * Created by mleduc on 18/12/15.
 */
public class TypeDefEquality {

    private final ObservableInstanceFactory observableInstanceFactory = new ObservableInstanceFactory();
    /**
     * @param previous The previous instance
     * @param next     The current instance
     * @return True if previous and next shares the same type definition.
     */
    public Boolean typeDefEquals(Instance previous, Instance next) {
        final Observable<TypeDefinition> previousTypeDefinition = observableInstanceFactory.getTypeDefObservable(previous);
        final Observable<TypeDefinition> nextTypeDefinition = observableInstanceFactory.getTypeDefObservable(next);
        return Observable.zip(nextTypeDefinition, previousTypeDefinition, new Func2<TypeDefinition, TypeDefinition, Boolean>() {
            @Override
            public Boolean call(TypeDefinition typeDef0, TypeDefinition typeDef1) {
                return typeDef0.getName().equals(typeDef1.getName()) && typeDef0.getVersion().equals(typeDef1.getVersion());
            }
        }).toBlocking().firstOrDefault(true);
    }
}
