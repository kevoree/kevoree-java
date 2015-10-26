package org.kevoree.server;

import org.kevoree.modeling.drivers.leveldb.LevelDbContentDeliveryDriver;
import org.kevoree.modeling.drivers.websocket.gateway.WebSocketGateway;

import java.io.IOException;

/**
 * Created by duke on 26/10/15.
 */
public class App {

    public static void main(String[] args) throws IOException {
        LevelDbContentDeliveryDriver db = new LevelDbContentDeliveryDriver("db");
        db.connect(throwable -> {
            WebSocketGateway gateway = WebSocketGateway.expose(db, 3080);
            gateway.start();
            System.out.println("Peer exposed to port 3080");

        });
    }

}
