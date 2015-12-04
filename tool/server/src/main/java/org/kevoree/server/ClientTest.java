package org.kevoree.server;


import org.KevoreeModel;
import org.KevoreeView;
import org.kevoree.Model;
import org.kevoree.modeling.cdn.KContentDeliveryDriver;
import org.kevoree.modeling.memory.manager.DataManagerBuilder;
import org.kevoree.server.cdd.WSClientCDD;

import java.io.IOException;
import java.net.URI;

public class ClientTest {

    public static void main(String[] args) throws IOException {
        KContentDeliveryDriver cdd = new WSClientCDD(URI.create("ws://localhost:3080"));
        KevoreeModel kModel = new KevoreeModel(DataManagerBuilder.create().withContentDeliveryDriver(cdd).build());
        kModel.connect(e -> {
            if (e != null) {
                System.err.println("Problem");
            } else {
                System.out.println("connected!");
                KevoreeView kView = kModel.universe(0).time(0);
                Model model = kView.createModel();
                kModel.save(err -> {
                    if (err != null) {
                        Throwable throwable = (Throwable) err;
                        System.err.println("Oups = "+throwable.getMessage());
                    } else {
                        System.out.println("aight");
                    }
                });
            }
        });
    }

}
