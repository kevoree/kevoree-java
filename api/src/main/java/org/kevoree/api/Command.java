package org.kevoree.api;

public interface Command {

    void run(Core core, Object ctx);

}
