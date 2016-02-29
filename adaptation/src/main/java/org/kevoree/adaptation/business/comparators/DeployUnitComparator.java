package org.kevoree.adaptation.business.comparators;

import com.github.zafarkhaja.semver.Version;
import org.kevoree.DeployUnit;

import java.util.Comparator;

/**
 * Created by mleduc on 18/12/15.
 */
public class DeployUnitComparator implements Comparator<DeployUnit> {
    @Override
    public int compare(DeployUnit o1, DeployUnit o2) {
        if(o1 != null && o2 != null) {
            final Version v1 = Version.valueOf(o1.getVersion());
            final Version v2 = Version.valueOf(o2.getVersion());
            return v1.compareTo(v2);
        } else {
            return 0;
        }
    }
}