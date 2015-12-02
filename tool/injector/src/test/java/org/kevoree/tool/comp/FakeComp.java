package org.kevoree.tool.comp;

import org.kevoree.annotations.Component;
import org.kevoree.annotations.inject.KevoreeInject;
import org.kevoree.api.context.ComponentContext;

@Component
public class FakeComp {

    @KevoreeInject
    private ComponentContext context;

    public ComponentContext getContext() {
        return context;
    }
}