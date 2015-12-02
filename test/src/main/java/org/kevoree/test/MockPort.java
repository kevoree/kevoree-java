package org.kevoree.test;

import org.kevoree.api.OutputPort;

import java.util.concurrent.CountDownLatch;
import java.util.concurrent.TimeUnit;

/**
 *
 * Created by leiko on 12/1/15.
 */
public class MockPort implements OutputPort {

    private static final int TIMEOUT = 10000;

    private String name;
    private int msgCount;
    private int expectedMsgCount;
    private CountDownLatch countDown;
    private int timeout;

    public MockPort(String name) {
        this.name = name;
        this.msgCount = 0;
        this.expectedMsgCount = 0;
        this.timeout = TIMEOUT;
    }

    public MockPort toBeCalled(int callCounts) {
        this.expectedMsgCount = callCounts;
        this.countDown = new CountDownLatch(callCounts);
        return this;
    }

    public void async(int timeout) {
        this.timeout = timeout;
    }

    public void async() {
        this.async(TIMEOUT);
    }

    public void asyncVerify() throws InterruptedException {
        this.countDown.await(timeout, TimeUnit.MILLISECONDS);
        verify();
    }

    public void verify() {
        if (msgCount != expectedMsgCount) {
            throw new AssertionError("OutputPort "+name+" (expected="+expectedMsgCount+", actual="+msgCount+")");
        }
    }

    @Override
    public void send(String s) {
        msgCount++;
        if (this.countDown != null) {
            this.countDown.countDown();
        }
    }
}
