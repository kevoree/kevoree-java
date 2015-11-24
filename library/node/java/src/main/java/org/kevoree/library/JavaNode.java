package org.kevoree.library;


import org.kevoree.core.api.Command;
import org.kevoree.core.api.annotations.Node;
import org.kevoree.library.command.AddInstance;
import org.kevoree.library.command.RemoveInstance;

/**
 * Created by mleduc on 23/11/15.
 */
@Node
public class JavaNode implements org.kevoree.core.api.Node {

    private final Command addInstance = new AddInstance();
    private final Command removeInstance = new RemoveInstance();

    @Override
    public Command getAddInstance() {
        return addInstance;
    }

    @Override
    public Command getRemoveInstance() {
        return removeInstance;
    }

}
