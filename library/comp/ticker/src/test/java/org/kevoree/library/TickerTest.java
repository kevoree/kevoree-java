package org.kevoree.library;

import org.easymock.EasyMock;
import org.easymock.EasyMockRunner;
import org.easymock.Mock;
import org.easymock.TestSubject;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.kevoree.annotations.params.IntParam;
import org.kevoree.api.OutputPort;
import org.kevoree.test.MockComponent;
import org.kevoree.test.exception.CreateMockException;
import org.kevoree.test.exception.SetParamException;

import java.lang.reflect.Field;
import java.util.Collections;
import java.util.concurrent.ScheduledExecutorService;

/**
 *
 * Created by leiko on 11/30/15.
 */
@RunWith(EasyMockRunner.class)
public class TickerTest {

    @TestSubject
    private Ticker ticker;

    @Mock
    private OutputPort tick;

    @Mock
    private ScheduledExecutorService executor;

    public TickerTest() throws Exception {
        MockComponent<Ticker> mock = new MockComponent<>(Ticker.class).setParam("period", 1);
        //System.out.println(mock.toString());
        ticker = mock.get();
    }

    @Test
    public void testStart() throws InterruptedException, NoSuchFieldException, IllegalAccessException {
        tick.send(EasyMock.anyString());
        EasyMock.replay(tick);

        ticker.start();
        Thread.sleep(2);
        EasyMock.verify(tick);
    }

    @Test
    public void testStop() throws InterruptedException {
        EasyMock.expect(executor.shutdownNow()).andReturn(Collections.emptyList());
        EasyMock.replay(executor);

        ticker.stop();
        EasyMock.verify(executor);
    }
}
