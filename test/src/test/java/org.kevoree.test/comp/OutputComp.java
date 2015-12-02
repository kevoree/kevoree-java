package org.kevoree.test.comp;

import org.kevoree.annotations.Component;
import org.kevoree.annotations.Output;
import org.kevoree.api.OutputPort;

/**
 *
 * Created by leiko on 12/1/15.
 */
@Component
public class OutputComp {

    @Output
    private OutputPort port;

    @Output
    private OutputPort port2;

    public void start() {
        port.send("foo");
    }

    public void asyncStart() {
        new Thread(() -> {
            try {
                Thread.sleep(200);
                port.send("bar");
            } catch (InterruptedException e) {
                e.printStackTrace();
            }

        }).start();
    }

    public void complexAsyncStart() {
        start();
        asyncStart();
        new Thread(() -> {
            try {
                Thread.sleep(500);
                port2.send("yolo");
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
        }).start();
    }
}