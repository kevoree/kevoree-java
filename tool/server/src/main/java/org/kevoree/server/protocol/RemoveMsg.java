package org.kevoree.server.protocol;

import java.util.UUID;

/**
 *
 * Created by leiko on 12/4/15.
 */
public class RemoveMsg {

    public static final String ACTION = "remove";

    private String action;
    private String id;
    private long[] keys;

    public RemoveMsg(long[] keys) {
        this.action = ACTION;
        this.id = UUID.randomUUID().toString();
        this.keys = keys;
    }

    public String getId() {
        return id;
    }

    public long[] getKeys() {
        return keys;
    }

    public String getAction() {
        return action;
    }
}
