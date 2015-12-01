package org.kevoree.test.comp;

import org.kevoree.annotations.Component;
import org.kevoree.annotations.params.BooleanParam;
import org.kevoree.annotations.params.IntParam;

/**
 *
 * Created by leiko on 12/1/15.
 */
@Component
public class FakeComp {

    @IntParam
    private int intVal = 42;

    @BooleanParam
    private boolean boolVal = false;

    public int getIntVal() {
        return intVal;
    }

    public boolean isBoolVal() {
        return boolVal;
    }
}
