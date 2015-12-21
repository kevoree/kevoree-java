package org.kevoree.adaptation.business.predicates;

import org.kevoree.Channel;
import org.kevoree.adaptation.business.comparators.TypeDefEquality;
import org.kevoree.adaptation.business.functional.Predicate;
import org.kevoree.adaptation.business.functional.PredicateFactory;

import java.util.Objects;

/**
 * Created by mleduc on 18/12/15.
 */
public class ChannelPredicateFactory implements PredicateFactory<Channel> {

    private final TypeDefEquality typeDefEquality = new TypeDefEquality();

    @Override
    public Predicate<? super Channel> get(final Channel a) {
        return new Predicate<Channel>() {
            @Override
            public boolean test(Channel b) {
                return Objects.equals(a.getName(), b.getName()) && typeDefEquality.typeDefEquals(a, b);
            }
        };
    }
}
