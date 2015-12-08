package org.kevoree.test.comp;

import org.kevoree.annotations.params.Max;
import org.kevoree.annotations.params.Min;
import org.kevoree.annotations.params.Multiline;
import org.kevoree.annotations.params.Param;

/**
 *
 * Created by leiko on 12/1/15.
 */
public class SubFakeComp extends FakeComp {

    @Param
    @Multiline
    private String stringVal;

    @Param
    @Min(0)
    @Max(10)
    private int intVal;

    public String getStringVal() {
        return stringVal;
    }

    @Override
    public int getIntVal() {
        return intVal;
    }
}
