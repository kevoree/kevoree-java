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

    public void start() {
        port.send("foo");
    }

    public void asyncStart() {
        new Thread(() -> {
            try {
                Thread.sleep(1000);
                port.send("bar");
            } catch (InterruptedException e) {
                e.printStackTrace();
            }

        }).start();
    }
}
