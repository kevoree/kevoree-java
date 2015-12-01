package org.kevoree.test;

import org.kevoree.api.OutputPort;

/**
 *
 * Created by leiko on 12/1/15.
 */
public class MockPort implements OutputPort {

    private String name;
    private int msgCount;

    public MockPort(String name) {
        this.name = name;
        this.msgCount = 0;
    }

    public void toSendAnyString() {
        if (msgCount == 0) {
            throw new AssertionError("OutputPort "+name+" should have sent one message. Got 0");
        }
        System.out.println("TO SEND ANY STRING");
    }

    public void toSendAnyString(int timeout) throws InterruptedException {
        //Thread.sleep(timeout);
        Thread.currentThread().join(timeout);
        toSendAnyString();
    }

    @Override
    public void send(String s) {
        msgCount++;
    }
}
