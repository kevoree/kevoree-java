package org.kevoree.adaptation.business.comparators;

import org.kevoree.Param;
import rx.functions.Func2;

/**
 * Created by mleduc on 18/12/15.
 */
public class ParamComparator implements Func2<Param, Param, Integer> {
    @Override
    public Integer call(Param param, Param param2) {
        return param.getName().compareTo(param2.getName());
    }
}
