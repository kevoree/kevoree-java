package org.kevoree.test;

import org.junit.Before;
import org.junit.Test;
import org.kevoree.test.comp.SubFakeComp;
import org.kevoree.test.exception.CreateMockException;
import org.kevoree.test.exception.SetParamException;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertTrue;

/**
 *
 * Created by leiko on 12/1/15.
 */
public class TestSubFakeComp {

    private MockComponent<SubFakeComp> mock;

    @Before
    public void setUp() throws CreateMockException, SetParamException {
        mock = new MockComponent<>(SubFakeComp.class);
    }

    @Test
    public void testGet() throws CreateMockException {
        SubFakeComp comp = mock.get();
        assertNotNull(comp);
    }

    @Test
    public void testCustomAttributes() throws CreateMockException, SetParamException {
        SubFakeComp comp = mock.setParam("intVal", 4)
                .setParam("boolVal", false)
                .setParam("stringVal", "foo")
                .get();
        assertNotNull(comp);
        assertEquals(comp.getIntVal(), 4);
        assertEquals(comp.isBoolVal(), false);
        assertEquals(comp.getStringVal(), "foo");
    }

    @Test
    public void testMultilineStrParam() throws CreateMockException, SetParamException {
        SubFakeComp comp = mock.get();
        assertTrue(comp.getStringVal().contains("\n"));
    }
}
