package org.kevoree.library.command;

import org.kevoree.core.api.Command;
import org.kevoree.core.api.Core;

public class RemoveInstance implements Command {
    @Override
    public void run(Core core, Object ctx) {
        System.out.println("RemoveInstance "+ctx.toString());
    }
}
