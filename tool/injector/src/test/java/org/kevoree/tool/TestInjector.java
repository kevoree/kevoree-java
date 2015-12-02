package org.kevoree.tool;

import org.junit.Before;
import org.junit.Test;
import org.kevoree.Component;
import org.kevoree.api.context.ComponentContext;
import org.kevoree.tool.comp.ComponentContextImpl;
import org.kevoree.tool.comp.FakeComp;

import static org.junit.Assert.*;

/**
 *
 * Created by leiko on 12/2/15.
 */
public class TestInjector {

    private static final String NODE_NAME = "node0";
    private static final String NAME = "comp";
    private static final Component INSTANCE = null;

    private Injector injector;
    private FakeComp comp;

    @Before
    public void setUp() {
        this.injector = new Injector();
        this.comp = new FakeComp();
        this.injector.register(ComponentContext.class, new ComponentContextImpl(NAME, INSTANCE, NODE_NAME));
    }

    @Test
    public void testInject() {
        this.injector.inject(this.comp);
        ComponentContext ctx = this.comp.getContext();

        assertEquals(ctx.getName(), NAME);
        assertEquals(ctx.getNodeName(), NODE_NAME);
        assertEquals(ctx.getInstance(), INSTANCE);
    }
}
