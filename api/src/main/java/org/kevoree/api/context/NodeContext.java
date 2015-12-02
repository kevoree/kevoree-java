package org.kevoree.api.context;

import org.kevoree.Node;

/**
 *
 * Created by leiko on 12/2/15.
 */
public interface NodeContext extends Context {

    Node getInstance();
}
