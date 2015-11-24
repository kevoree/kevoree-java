package org.kevoree.library.command;


import org.kevoree.api.Command;
import org.kevoree.api.Core;

public class AddInstance implements Command {
    @Override
    public void run(Core core, Object ctx) {
        System.out.println("AddInstance "+ctx.toString());
    }
}
