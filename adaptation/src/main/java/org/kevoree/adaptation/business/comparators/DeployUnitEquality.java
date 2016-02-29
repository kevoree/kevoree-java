package org.kevoree.adaptation.business.comparators;

import org.kevoree.DeployUnit;
import org.kevoree.adaptation.business.functional.Predicate;
import org.kevoree.adaptation.business.functional.PredicateFactory;

import java.util.Objects;

/**
 * Created by mleduc on 21/12/15.
 */
public class DeployUnitEquality implements PredicateFactory<DeployUnit> {
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
