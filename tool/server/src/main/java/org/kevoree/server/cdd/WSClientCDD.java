package org.kevoree.server.cdd;

import com.google.gson.*;
import fr.braindead.websocket.client.WebSocketClient;
import org.kevoree.modeling.KCallback;
import org.kevoree.modeling.cdn.KContentDeliveryDriver;
import org.kevoree.modeling.cdn.KContentUpdateListener;
import org.kevoree.modeling.message.KMessage;
import org.kevoree.server.protocol.*;

import java.io.IOException;
import java.net.URI;
import java.util.HashMap;
import java.util.Map;

/**
 *
 * Created by leiko on 12/4/15.
 */
public class WSClientCDD implements KContentDeliveryDriver {

    private URI uri;
    private WebSocketClient client;
    private Map<String, KCallback<String[]>> elemsMap = new HashMap<>();
    private Map<String, KCallback<Short>> atomicMap = new HashMap<>();
    private Map<String, KCallback<Throwable>> throwableMap = new HashMap<>();
    private Gson gson;

    public WSClientCDD(URI uri) {
        this.uri = uri;
        this.gson = new Gson();
    }

    @Override
    public void get(long[] keys, KCallback<String[]> callback) {
        if (this.client == null) {
            System.err.println("get failed: client null");
        } else {
            GetMsg msg = new GetMsg(keys);
            this.elemsMap.put(msg.getId(), callback);
            System.out.println(">> "+gson.toJson(msg));
            this.client.send(gson.toJson(msg));
        }
    }

    @Override
    public void atomicGetIncrement(long[] keys, KCallback<Short> callback) {
        if (this.client == null) {
            System.err.println("atomicGet failed: client null");
        } else {
            AtomicGetMsg msg = new AtomicGetMsg(keys);
            this.atomicMap.put(msg.getId(), callback);
            System.out.println(">> "+gson.toJson(msg));
            this.client.send(gson.toJson(msg));
        }
    }

    @Override
    public void put(long[] keys, String[] values, KCallback<Throwable> callback, int excludeListener) {
        if (this.client == null) {
            System.err.println("put failed: client null");
        } else {
            PutMsg msg = new PutMsg(keys, values, excludeListener);
            this.throwableMap.put(msg.getId(), callback);
            System.out.println(">> "+gson.toJson(msg));
            this.client.send(gson.toJson(msg));
        }
    }

    @Override
    public void remove(long[] keys, KCallback<Throwable> callback) {
        if (this.client == null) {
            System.err.println("remove failed: client null");
        } else {
            RemoveMsg msg = new RemoveMsg(keys);
            this.throwableMap.put(msg.getId(), callback);
            System.out.println(">> "+gson.toJson(msg));
            this.client.send(gson.toJson(msg));
        }
    }

    @Override
    public void connect(final KCallback<Throwable> callback) {
        try {
            this.client = new WebSocketClient(this.uri) {
                @Override
                public void onOpen() {
                    callback.on(null);
                }

                @Override
                public void onMessage(String msg) {
                    System.out.println("<< "+msg);
                    JsonParser parser = new JsonParser();
                    try {
                        JsonObject obj = parser.parse(msg).getAsJsonObject();
                        if (obj.has("action")) {
                            switch (obj.get("action").getAsString()) {
                                case ElemsAnswerMsg.ACTION:
                                    ElemsAnswerMsg getMsg = gson.fromJson(msg, ElemsAnswerMsg.class);
                                    KCallback<String[]> getCb = elemsMap.get(getMsg.getId());
                                    elemsMap.remove(getMsg.getId());
                                    if (getCb != null) {
                                        getCb.on(getMsg.getElem());
                                    }
                                    break;

                                case AtomicIntAnswerMsg.ACTION:
                                    AtomicIntAnswerMsg atomicGetMsg = gson.fromJson(msg, AtomicIntAnswerMsg.class);
                                    KCallback<Short> atomicGetCb = atomicMap.get(atomicGetMsg.getId());
                                    atomicMap.remove(atomicGetMsg.getId());
                                    if (atomicGetCb != null) {
                                        atomicGetCb.on(atomicGetMsg.getAtomicInc());
                                    }
                                    break;

                                case ThrowableAnswerMsg.ACTION:
                                    ThrowableAnswerMsg putMsg = gson.fromJson(msg, ThrowableAnswerMsg.class);
                                    KCallback<Throwable> putCb = throwableMap.get(putMsg.getId());
                                    throwableMap.remove(putMsg.getId());
                                    if (putCb != null) {
                                        Throwable throwable = null;
                                        if (putMsg.getError() != null) {
                                            throwable = new Throwable(putMsg.getError());
                                        }
                                        putCb.on(throwable);
                                    }
                                    break;
                            }
                        } else {
                            System.err.println("Received message is malformed");
                        }
                    } catch (IllegalStateException | JsonSyntaxException e) {
                        System.err.println("Received message is not a JsonObject");
                    }
                }

                @Override
                public void onClose(int code, String reason) {

                }

                @Override
                public void onError(Exception e) {
                    callback.on(e);
                }
            };
        } catch (IOException e) {
            callback.on(e);
        }
    }

    @Override
    public void close(KCallback<Throwable> callback) {
        if (this.client != null) {
            try {
                this.client.close();
                callback.on(null);
            } catch (IOException e) {
                callback.on(e);
            }
        } else {
            callback.on(null);
        }
    }

    @Override
    public int addUpdateListener(KContentUpdateListener interceptor) {
        return 0;
    }

    @Override
    public void removeUpdateListener(int id) {

    }

    @Override
    public String[] peers() {
        return new String[0];
    }

    @Override
    public void sendToPeer(String peer, KMessage message, KCallback<KMessage> callback) {

    }
}
