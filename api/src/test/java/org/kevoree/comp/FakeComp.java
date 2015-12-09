package org.kevoree.comp;

import org.kevoree.annotations.Component;
import org.kevoree.annotations.params.Param;

/**
 *
 * Created by leiko on 12/2/15.
 */
@Component(version = 42)
public class FakeComp {

    public enum MyEnum { ONE, TWO, THREE }

    @Param
    private MyEnum myEnum = MyEnum.TWO;
}
