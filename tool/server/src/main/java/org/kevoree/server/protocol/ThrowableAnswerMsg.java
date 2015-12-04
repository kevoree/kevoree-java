package org.kevoree.server.protocol;

/**
 *
 * Created by leiko on 12/4/15.
 */
public class ThrowableAnswerMsg {

    public static final String ACTION = "throwableAnswer";

    private String action;
    private String id;
    private String error;

    public ThrowableAnswerMsg(String id, String error) {
        this.action = ACTION;
        this.id = id;
        this.error = error;
    }

    public String getId() {
        return id;
    }

    public String getError() {
        return error;
    }

    public String getAction() {
        return action;
    }
}
