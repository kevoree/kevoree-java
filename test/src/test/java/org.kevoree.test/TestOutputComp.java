package org.kevoree.test;

import org.junit.Before;
import org.junit.Test;
import org.kevoree.test.comp.OutputComp;

/**
 *
 * Created by leiko on 12/1/15.
 */
public class TestOutputComp {

    private MockComponent<OutputComp> mock;

    @Before
    public void setUp() throws Exception {
        mock = new MockComponent<>(OutputComp.class);
    }

    @Test
    public void testStart() {
        OutputComp comp = mock.get();
        mock.expectPort("port").toBeCalled(1);
        comp.start();
        mock.verifyPorts();
    }

    @Test
    public void testAsyncStart() throws InterruptedException {
        OutputComp comp = mock.get();
        mock.expectPort("port").toBeCalled(1).async();
        comp.asyncStart();
        mock.verifyAsyncPorts();
    }
}
