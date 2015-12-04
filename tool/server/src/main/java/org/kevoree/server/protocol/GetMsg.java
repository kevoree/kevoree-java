package org.kevoree.server.protocol;

import java.util.UUID;

/**
 *
 * Created by leiko on 12/4/15.
 */
public class GetMsg {

    public static final String ACTION = "get";

    private String action;
    private String id;
    private long[] keys;

    public GetMsg(long[] keys) {
        this.action = ACTION;
        this.id = UUID.randomUUID().toString();
        this.keys = keys;
    }

    public long[] getKeys() {
        return keys;
    }

    public String getId() {
        return id;
    }

    public String getAction() {
        return action;
    }
}
