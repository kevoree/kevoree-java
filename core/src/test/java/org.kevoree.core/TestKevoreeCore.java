package org.kevoree.core;

import org.junit.Before;
import org.junit.Test;

public class TestKevoreeCore {

    private KevoreeCore core;

    @Before
    public void setUp() {
        core = new KevoreeCore();
    }

    @Test
    public void testStart() {
        core.boot("node0", 9000);
    }
}