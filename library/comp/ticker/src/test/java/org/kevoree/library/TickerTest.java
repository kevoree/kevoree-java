package org.kevoree.library;

import org.junit.Before;
import org.junit.Test;
import org.kevoree.test.MockComponent;

/**
 *
 * Created by leiko on 11/30/15.
 */
public class TickerTest {

    private MockComponent<Ticker> mock;
    private Ticker ticker;

    @Before
    public void setUp() throws Exception {
        mock = new MockComponent<>(Ticker.class);
        ticker = mock.setParam("period", 100).get();
    }

    @Test
    public void testStart() throws Exception {
        mock.expectPort("tick").toBeCalled(1).async();
        ticker.start();
        mock.verifyPorts();
    }

    @Test(expected = IllegalArgumentException.class)
    public void testWrongPeriod() throws Exception {
        ticker = mock.setParam("period", 0).get();
        ticker.start();
    }
}
