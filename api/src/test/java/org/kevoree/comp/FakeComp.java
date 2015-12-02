package org.kevoree.comp;

import org.kevoree.annotations.Component;
import org.kevoree.annotations.params.ChoiceParam;

/**
 *
 * Created by leiko on 12/2/15.
 */
@Component
public class FakeComp {

    public enum MyEnum { ONE, TWO, THREE }

    @ChoiceParam
    private MyEnum myEnum = MyEnum.TWO;
}
