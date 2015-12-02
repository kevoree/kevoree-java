package org.kevoree.api.context;

import org.kevoree.Group;

/**
 *
 * Created by leiko on 12/2/15.
 */
public interface GroupContext extends Context {

    String getName();

    Group getInstance();
}
