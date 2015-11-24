package org.kevoree.core.api;

public interface Command {

    void run(Core core, Object ctx);

}
