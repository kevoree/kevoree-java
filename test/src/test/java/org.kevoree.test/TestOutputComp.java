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
    private OutputComp comp;

    @Before
    public void setUp() throws Exception {
        mock = new MockComponent<>(OutputComp.class);
        comp = mock.get();
    }

    @Test
    public void testStart() throws InterruptedException {
        mock.expectPort("port").toBeCalled(1);

        comp.start();

        mock.verifyPorts();
    }

    @Test
    public void testAsyncStart() throws InterruptedException {
        mock.expectPort("port").toBeCalled(1).async();

        comp.asyncStart();

        mock.verifyPorts();
    }

    @Test
    public void testComplexAsyncStart() throws InterruptedException {
        mock.expectPort("port").toBeCalled(1);
        mock.expectPort("port").toBeCalled(1).async();
        mock.expectPort("port2").toBeCalled(1).async();

        comp.complexAsyncStart();

        mock.verifyPorts();
    }
}
