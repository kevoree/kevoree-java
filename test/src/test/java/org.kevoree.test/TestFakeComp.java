package org.kevoree.test;

import org.junit.Before;
import org.junit.Test;
import org.kevoree.test.comp.FakeComp;
import org.kevoree.test.exception.CreateMockException;
import org.kevoree.test.exception.SetFieldException;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;

/**
 *
 * Created by leiko on 12/1/15.
 */
public class TestFakeComp {

    private MockComponent<FakeComp> mock;

    @Before
    public void setUp() throws CreateMockException, SetFieldException {
        mock = new MockComponent<>(FakeComp.class);
    }

    @Test
    public void testGet() throws Exception {
        FakeComp comp = mock.get();
        assertNotNull(comp);
    }

    @Test
    public void testCustomAttributes() throws Exception {
        FakeComp comp = mock.setParam("intVal", 0)
                            .setParam("boolVal", true)
                            .get();
        assertNotNull(comp);
        assertEquals(comp.getIntVal(), 0);
        assertEquals(comp.isBoolVal(), true);
    }
}
