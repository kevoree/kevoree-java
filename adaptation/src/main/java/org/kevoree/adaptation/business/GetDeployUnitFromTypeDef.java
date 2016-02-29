package org.kevoree.adaptation.business;

import org.kevoree.DeployUnit;
import org.kevoree.TypeDefinition;
import org.kevoree.adaptation.observable.ObservableTypeDefinitionFactory;
import rx.functions.Func1;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Objects;

/**
 * Created by mleduc on 21/12/15.
 */
class GetDeployUnitFromTypeDef implements Func1<TypeDefinition, DeployUnit> {
    private final String platform;

    private final ObservableTypeDefinitionFactory observableTypeDefinitionFactory = new ObservableTypeDefinitionFactory();

    public GetDeployUnitFromTypeDef(String platform) {
        this.platform = platform;
    }

    @Override
    public DeployUnit call(TypeDefinition typeDefinition) {
        final List<DeployUnit> first = observableTypeDefinitionFactory.getDeployUnitObservable(typeDefinition).toList().toBlocking().first();

        // first we select the typeDef related to the node's platform.
        final List<DeployUnit> plat = filteringByPlatform(first);

        // then we sort the according to their semver.
        Collections.sort(plat, new org.kevoree.adaptation.business.comparators.DeployUnitComparator());

        // and we select the latest.
        Collections.reverse(plat);
        return getFirstOrNulll(plat);
    }

    private DeployUnit getFirstOrNulll(List<DeployUnit> plat) {
        final DeployUnit ret;
        if (plat.isEmpty()) {
            ret = null;
        } else {
            ret = plat.get(0);
        }
        return ret;
    }

    private List<DeployUnit> filteringByPlatform(List<DeployUnit> first) {
        final List<DeployUnit> plat = new ArrayList<>();
        for (DeployUnit du : first) {
            if (Objects.equals(platform, du.getPlatform())) {
                plat.add(du);
            }
        }
        return plat;
    }
}
