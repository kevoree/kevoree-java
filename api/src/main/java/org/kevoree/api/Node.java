package org.kevoree.core.api;

/**
 * Created by mleduc on 24/11/15.
 */
public interface Node {
    Command getAddInstance();

    Command getRemoveInstance();
}
