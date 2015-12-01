package org.kevoree.library;

import org.easymock.EasyMock;
import org.easymock.EasyMockRunner;
import org.easymock.Mock;
import org.easymock.TestSubject;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.kevoree.annotations.params.IntParam;
import org.kevoree.api.OutputPort;

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
    private Ticker ticker = new Ticker();

    @Mock
    private OutputPort tick;

    @Mock
    private ScheduledExecutorService executor;

    @Test
    public void testStart() throws InterruptedException, NoSuchFieldException, IllegalAccessException {
        tick.send(EasyMock.anyString());
        EasyMock.replay(tick);

        for (Field field : ticker.getClass().getFields()) {
            if (field.isAnnotationPresent(IntParam.class)) {
                field.setAccessible(true);
                field.set(ticker, 500);
                field.setAccessible(false);
            }
        }
        ticker.start();
        // dirty
        Thread.sleep(1000);
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
