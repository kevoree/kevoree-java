package org.kevoree.adaptation.business.comparators;

import org.kevoree.Group;
import org.kevoree.Instance;
import org.kevoree.adaptation.business.functional.Predicate;
import org.kevoree.adaptation.business.functional.PredicateFactory;

/**
 * Created by mleduc on 21/12/15.
 */
public class InstanceEqualityFactory<T extends Instance> implements PredicateFactory<T> {

    private final String platform;

    public InstanceEqualityFactory(String platform) {
        this.platform = platform;
    }

    @Override
    public Predicate<? super Instance> get(final Instance a) {
        return new Predicate<Instance>() {
            @Override
            public boolean test(Instance b) {
                return InstanceEquality.componentEquality(a, b, platform);
            }
        };
    }
}
