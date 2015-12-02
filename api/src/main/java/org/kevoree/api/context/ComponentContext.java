package org.kevoree.api.context;

import org.kevoree.Component;

/**
 *
 * Created by leiko on 12/2/15.
 */
public interface ComponentContext extends Context {

    String getName();

    Component getInstance();
}
