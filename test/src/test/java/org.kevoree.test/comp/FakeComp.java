package org.kevoree.test.comp;

import org.kevoree.annotations.Component;
import org.kevoree.annotations.params.Param;

/**
 *
 * Created by leiko on 12/1/15.
 */
@Component(version = 42)
public class FakeComp {

    @Param
    private int intVal = 42;

    @Param
    private boolean boolVal = false;

    public int getIntVal() {
        return intVal;
    }

    public boolean isBoolVal() {
        return boolVal;
    }
}
