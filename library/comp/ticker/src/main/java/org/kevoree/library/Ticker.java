package org.kevoree.library;

import org.kevoree.annotations.*;
import org.kevoree.annotations.params.BooleanParam;
import org.kevoree.annotations.params.IntParam;
import org.kevoree.api.OutputPort;

import java.util.Random;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;

/**
 *
 * Created by leiko on 11/30/15.
 */
@Component(description = "TODO")
public class Ticker {

    @IntParam
    private int period = 3000;

    @BooleanParam
    private boolean random = false;

    @Output
    private OutputPort tick;

    private ScheduledExecutorService executor;

    @Start
    public void start() {
        executor = Executors.newSingleThreadScheduledExecutor();
        executor.scheduleAtFixedRate(() -> {
            String msg;
            if (random) {
                msg = String.valueOf(new Random().nextInt(Integer.MAX_VALUE));
            } else {
                msg = String.valueOf(System.currentTimeMillis());
            }
            System.out.println("send tick = "+tick);
            tick.send(msg);
        }, 0, period, TimeUnit.MILLISECONDS);
    }

    @Stop
    public void stop() {
        executor.shutdownNow();
    }

    @Update
    public void update() {
        stop();
        start();
    }
}
