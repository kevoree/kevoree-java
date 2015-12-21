package org.kevoree.adaptation.business.comparators;

import org.kevoree.Component;
import org.kevoree.DeployUnit;
import org.kevoree.adaptation.business.DiffUtil;
import rx.Observable;

import java.util.Objects;

/**
 * Created by mleduc on 21/12/15.
 */
public class ComponentEquality {
    public static boolean componentEquality(Component newComponent, Component prevComponent, String platform) {
        final boolean nameEquals = Objects.equals(prevComponent.getName(), newComponent.getName());
        final boolean sameTypeDef = new TypeDefEquality().typeDefEquals(prevComponent, newComponent);

        final DeployUnit first1 = DiffUtil.loadDeployUnitFromInstance(Observable.just(prevComponent), platform).toBlocking().firstOrDefault(null);
        final DeployUnit first = DiffUtil.loadDeployUnitFromInstance(Observable.just(newComponent), platform).toBlocking().firstOrDefault(null);
        final boolean sameDeployUnit = new org.kevoree.adaptation.business.comparators.DeployUnitComparator().compare(first1, first) == 0;
        return nameEquals && sameTypeDef && sameDeployUnit;
    }
}
