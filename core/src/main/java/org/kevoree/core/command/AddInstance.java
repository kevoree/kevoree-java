package org.kevoree.core.command;

import org.kevoree.core.api.Command;
import org.kevoree.core.api.Core;

public class AddInstance implements Command {
    @Override
    public void run(Core core, Object ctx) {
        System.out.println("AddInstance "+ctx.toString());
    }
}
