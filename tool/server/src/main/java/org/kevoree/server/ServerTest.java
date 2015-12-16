package org.kevoree.server;


import org.kevoree.modeling.KCallback;
import org.kevoree.modeling.cdn.KContentDeliveryDriver;
import org.kevoree.server.cdd.WSServerCDD;

import java.io.IOException;
import java.net.URI;

public class ServerTest {

    public static void main(String[] args) throws IOException {
        KContentDeliveryDriver cdd = new WSServerCDD(URI.create("ws://localhost:3080"));
        cdd.connect(new KCallback<Throwable>() {
            @Override
            public void on(Throwable e) {
                if (e != null) {
                    System.err.println("Error: "+e.getMessage());
                } else {
                    System.out.println("connected");
                }
            }
        });
    }

}
