package org.kevoree.server.cdd;

import com.google.gson.Gson;
import com.google.gson.JsonObject;
import com.google.gson.JsonParseException;
import com.google.gson.JsonParser;
import fr.braindead.websocket.server.WebSocketServer;
import fr.braindead.websocket.server.WebSocketServerImpl;
import org.kevoree.modeling.KCallback;
import org.kevoree.modeling.cdn.KContentDeliveryDriver;
import org.kevoree.modeling.cdn.KContentUpdateListener;
import org.kevoree.modeling.cdn.impl.MemoryContentDeliveryDriver;
import org.kevoree.modeling.message.KMessage;
import org.kevoree.server.protocol.*;

import java.net.URI;

/**
 *
 * Created by leiko on 12/4/15.
 */
public class WSServerCDD implements KContentDeliveryDriver {

    private KContentDeliveryDriver cdd;

    /**
     * Delegates CDD logic to a MemoryContentDeliveryDriver
     * @param uri used to start a WebSocket server gateway
     */
    public WSServerCDD(URI uri) {
        this(uri, new MemoryContentDeliveryDriver());
    }

    /**
     * Delegates CDD logic to the given one
     * @param uri used to start a WebSocket server gateway
     * @param cdd the real cdd to use
     */
    public WSServerCDD(URI uri, KContentDeliveryDriver cdd) {
        this.startServer(uri);
        this.cdd = cdd;
    }

    private void startServer(URI uri) {
        WebSocketServer server = new WebSocketServerImpl(uri);
        server.start();
        server.onConnect((path, client) -> client.onMessage(msg -> {
            System.out.println("<< "+msg);
            JsonParser parser = new JsonParser();
            try {
                JsonObject obj = parser.parse(msg).getAsJsonObject();
                Gson gson = new Gson();
                if (obj.has("action")) {
                    switch (obj.get("action").getAsString()) {
                        case GetMsg.ACTION:
                            GetMsg getMsg = gson.fromJson(msg, GetMsg.class);
                            this.cdd.get(getMsg.getKeys(), elems -> {
                                ElemsAnswerMsg answer = new ElemsAnswerMsg(getMsg.getId(), elems);
                                client.send(gson.toJson(answer));
                            });
                            break;

                        case PutMsg.ACTION:
                            PutMsg putMsg = gson.fromJson(msg, PutMsg.class);
                            this.cdd.put(putMsg.getKeys(), putMsg.getValues(), e -> {
                                String error = e != null ? e.getMessage() : null;
                                ThrowableAnswerMsg answer = new ThrowableAnswerMsg(putMsg.getId(), error);
                                client.send(gson.toJson(answer));
                            }, putMsg.getExcludeListener());
                            break;

                        case RemoveMsg.ACTION:
                            RemoveMsg removeMsg = gson.fromJson(msg, RemoveMsg.class);
                            this.cdd.remove(removeMsg.getKeys(), e -> {
                                String error = e != null ? e.getMessage() : null;
                                ThrowableAnswerMsg answer = new ThrowableAnswerMsg(removeMsg.getId(), error);
                                client.send(gson.toJson(answer));
                            });
                            break;

                        case AtomicGetMsg.ACTION:
                            AtomicGetMsg atomicGetMsg = gson.fromJson(msg, AtomicGetMsg.class);
                            this.cdd.atomicGetIncrement(atomicGetMsg.getKeys(), atomicInc -> {
                                AtomicIntAnswerMsg answer = new AtomicIntAnswerMsg(atomicGetMsg.getId(), atomicInc);
                                client.send(gson.toJson(answer));
                            });
                            break;
                    }
                } else {
                    System.err.println("Received message is malformed");
                }
            } catch (IllegalStateException | JsonParseException e) {
                System.err.println("Received message is not a JsonObject");
            }
        }));
    }

    @Override
    public void get(long[] keys, KCallback<String[]> callback) {
        this.cdd.get(keys, callback);
    }

    @Override
    public void atomicGetIncrement(long[] key, KCallback<Short> cb) {
        this.cdd.atomicGetIncrement(key, cb);
    }

    @Override
    public void put(long[] keys, String[] values, KCallback<Throwable> error, int excludeListener) {
        this.cdd.put(keys, values, error, excludeListener);
    }

    @Override
    public void remove(long[] keys, KCallback<Throwable> error) {
        this.cdd.remove(keys, error);
    }

    @Override
    public void connect(KCallback<Throwable> callback) {
        this.cdd.connect(callback);
    }

    @Override
    public void close(KCallback<Throwable> callback) {
        this.cdd.close(callback);
    }

    @Override
    public int addUpdateListener(KContentUpdateListener interceptor) {
        return this.cdd.addUpdateListener(interceptor);
    }

    @Override
    public void removeUpdateListener(int id) {
        this.cdd.removeUpdateListener(id);
    }

    @Override
    public String[] peers() {
        return this.cdd.peers();
    }

    @Override
    public void sendToPeer(String peer, KMessage message, KCallback<KMessage> callback) {
        this.cdd.sendToPeer(peer, message, callback);
    }
}
