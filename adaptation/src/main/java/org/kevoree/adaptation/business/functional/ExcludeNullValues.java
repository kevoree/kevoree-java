package org.kevoree.adaptation.business.functional;

import org.kevoree.DeployUnit;
import rx.functions.Func1;

/**
 * Created by mleduc on 21/12/15.
 */
public class ExcludeNullValues implements Func1<DeployUnit, Boolean> {
    @Override
    public Boolean call(DeployUnit deployUnit) {
        return deployUnit != null;
    }
}
