package org.kevoree.library;

import org.kevoree.annotations.Component;
import org.kevoree.annotations.Start;
import org.kevoree.annotations.Stop;
import org.kevoree.annotations.Update;
import org.kevoree.annotations.params.BooleanParam;
import org.kevoree.annotations.params.IntParam;

/**
 *
 * Created by leiko on 11/30/15.
 */
@Component(description = "TODO")
public class Ticker {

    @IntParam
    private int period;

    @BooleanParam
    private boolean random;

    @Start
    public void start() {

    }

    @Stop
    public void stop() {

    }

    @Update
    public void update() {

    }
}
