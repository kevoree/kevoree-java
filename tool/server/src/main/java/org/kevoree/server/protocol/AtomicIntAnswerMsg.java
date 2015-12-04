package org.kevoree.server.protocol;

/**
 *
 * Created by leiko on 12/4/15.
 */
public class AtomicIntAnswerMsg {

    public static final String ACTION = "atomicIncAnswer";

    private String action;
    private String id;
    private Short atomicInc;

    public AtomicIntAnswerMsg(String id, Short atomicInc) {
        this.action = ACTION;
        this.id = id;
        this.atomicInc = atomicInc;
    }

    public Short getAtomicInc() {
        return atomicInc;
    }

    public String getId() {
        return id;
    }

    public String getAction() {
        return action;
    }
}
