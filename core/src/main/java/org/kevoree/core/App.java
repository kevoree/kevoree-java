package org.kevoree.core;

import org.kevoree.core.kernel.KevoreeCore;

import java.util.Random;

/**
 * Created by duke on 26/10/15.
 */
public class App {

    public static void main(String[] args) {
        Random random = new Random();
        KevoreeCore kernel = new KevoreeCore();
        kernel.boot("ws://localhost:3080/shared", "node_" + Math.abs(random.nextInt()));
    }
}
