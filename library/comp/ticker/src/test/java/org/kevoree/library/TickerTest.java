package org.kevoree.library;

import org.easymock.EasyMock;
import org.easymock.EasyMockRunner;
import org.easymock.Mock;
import org.easymock.TestSubject;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.kevoree.api.OutputPort;

/**
 *
 * Created by leiko on 11/30/15.
 */
@RunWith(EasyMockRunner.class)
public class TickerTest {

    @TestSubject
    private Ticker ticker = new Ticker();

    @Mock
    private OutputPort tick;

    @Before
    public void setUp() {
    }

    @Test
    public void testStart() throws InterruptedException {
        tick.send(EasyMock.anyString());

        EasyMock.replay(tick);

        ticker.start();

        // dirty
        Thread.sleep(4000);

        EasyMock.verify(tick);
    }
}
