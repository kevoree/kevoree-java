package org.kevoree.adaptation.business.comparators;

import org.kevoree.Group;
import org.kevoree.adaptation.business.DiffUtil;
import org.kevoree.adaptation.business.functional.Predicate;
import org.kevoree.adaptation.business.functional.PredicateFactory;

/**
 * Created by mleduc on 21/12/15.
 */
public class GroupEquality implements PredicateFactory<Group> {

    private TypeDefEquality typeDefEquality = new TypeDefEquality();

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
