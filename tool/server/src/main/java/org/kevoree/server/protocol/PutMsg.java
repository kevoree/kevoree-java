package org.kevoree.server.protocol;

import java.util.UUID;

/**
 *
 * Created by leiko on 12/4/15.
 */
public class PutMsg {

    public static final String ACTION = "put";

    private String action;
    private String id;
    private long[] keys;
    private String[] values;
    private int excludeListener;

    public PutMsg(long[] keys, String[] values, int excludeListener) {
        this.action = ACTION;
        this.id = UUID.randomUUID().toString();
        this.keys = keys;
        this.values = values;
        this.excludeListener = excludeListener;
    }

    public String getId() {
        return id;
    }

    public long[] getKeys() {
        return keys;
    }

    public String[] getValues() {
        return values;
    }

    public String getAction() {
        return action;
    }

    public int getExcludeListener() {
        return excludeListener;
    }
}
