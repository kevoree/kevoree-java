package org.kevoree.server.protocol;

/**
 *
 * Created by leiko on 12/4/15.
 */
public class ElemsAnswerMsg {

    public static final String ACTION = "elemsAnswer";

    private String action;
    private String id;
    private String[] elem;

    public ElemsAnswerMsg(String id, String[] elem) {
        this.action = ACTION;
        this.id = id;
        this.elem = elem;
    }

    public String[] getElem() {
        return elem;
    }

    public String getId() {
        return id;
    }

    public String getAction() {
        return action;
    }
}
