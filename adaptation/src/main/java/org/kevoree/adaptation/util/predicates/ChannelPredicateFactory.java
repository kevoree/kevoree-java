package org.kevoree.adaptation.util.predicates;

import org.kevoree.Channel;
import org.kevoree.adaptation.util.DiffUtil;
import org.kevoree.adaptation.util.comparators.TypeDefEquality;
import org.kevoree.adaptation.util.functional.Predicate;
import org.kevoree.adaptation.util.functional.PredicateFactory;

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
