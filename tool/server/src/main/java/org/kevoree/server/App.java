package org.kevoree.server;

import io.undertow.Handlers;
import io.undertow.Undertow;
import io.undertow.server.handlers.resource.ClassPathResourceManager;
import org.kevoree.modeling.KCallback;
import org.kevoree.modeling.plugin.LevelDBPlugin;
import org.kevoree.modeling.plugin.WebSocketGateway;

import java.io.IOException;

public class App {

    public static void main(String[] args) throws IOException {
        final LevelDBPlugin db = new LevelDBPlugin("db");
        db.connect(new KCallback<Throwable>() {
            @Override
            public void on(Throwable throwable) {
                WebSocketGateway gateway = WebSocketGateway.expose(db, 3080);
                gateway.start();
                System.out.println("Peer exposed to port 3080");
                Undertow.builder().addHttpListener(3088, "0.0.0.0", Handlers.resource(new ClassPathResourceManager(App.class.getClassLoader()))).build().start();
                System.out.println("Dashboard exposed to port 3088");
            }
        });
    }

}