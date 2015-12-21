package org.kevoree.adaptation.business.comparators;

import org.kevoree.Node;

import java.util.Objects;

/**
 * Created by mleduc on 21/12/15.
 */
public class NodeEquality {
    private final static TypeDefEquality typeDefEquality = new TypeDefEquality();

    public static boolean nodeEquality(Node prev, Node current) {
        final String prevName = prev.getName();
        final String currentName = current.getName();
        final boolean nameEquality = Objects.equals(prevName, currentName);
        return nameEquality && typeDefEquality.typeDefEquals(prev, current);
    }
}
