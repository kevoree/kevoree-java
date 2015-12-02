package org.kevoree.tool.comp;

import org.kevoree.Component;
import org.kevoree.api.context.ComponentContext;

/**
 *
 * Created by leiko on 12/2/15.
 */
public class ComponentContextImpl implements ComponentContext {

    private String name;
    private Component instance;
    private String nodeName;

    public ComponentContextImpl(String name, Component instance, String nodeName) {
        this.name = name;
        this.instance = instance;
        this.nodeName = nodeName;
    }

    @Override
    public String getName() {
        return name;
    }

    @Override
    public Component getInstance() {
        return instance;
    }

    @Override
    public String getNodeName() {
        return nodeName;
    }
}
