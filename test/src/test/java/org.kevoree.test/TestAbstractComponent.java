package org.kevoree.test;

import org.junit.Test;
import org.kevoree.test.comp.AbstractComponent;
import org.kevoree.test.exception.CreateMockException;
import org.kevoree.test.exception.SetParamException;

/**
 *
 * Created by leiko on 12/1/15.
 */
public class TestAbstractComponent {

    private MockComponent<AbstractComponent> mock;

    @Test(expected = CreateMockException.class)
    public void setUp() throws CreateMockException, SetParamException {
        this.mock = new MockComponent<>(AbstractComponent.class);
    }
}
