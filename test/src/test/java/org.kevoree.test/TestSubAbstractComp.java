package org.kevoree.test;

import org.junit.Before;
import org.junit.Test;
import org.kevoree.test.comp.SubAbstractComp;
import org.kevoree.test.exception.CreateMockException;
import org.kevoree.test.exception.SetParamException;

import static org.junit.Assert.assertNotNull;

/**
 *
 * Created by leiko on 12/1/15.
 */
public class TestSubAbstractComp {

    private MockComponent<SubAbstractComp> mock;

    @Before
    public void setUp() throws CreateMockException, SetParamException {
        this.mock = new MockComponent<>(SubAbstractComp.class);
    }

    @Test
    public void testGet() {
        SubAbstractComp comp = this.mock.get();
        assertNotNull(comp);
    }
}
