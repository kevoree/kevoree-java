package org.kevoree.test.comp;

import org.kevoree.annotations.params.IntParam;
import org.kevoree.annotations.params.StringParam;

/**
 *
 * Created by leiko on 12/1/15.
 */
public class SubFakeComp extends FakeComp {

    @StringParam(multiline = true)
    private String stringVal;

    @IntParam(min = 0, max = 10)
    private int intVal;

    public String getStringVal() {
        return stringVal;
    }

    @Override
    public int getIntVal() {
        return intVal;
    }
}
