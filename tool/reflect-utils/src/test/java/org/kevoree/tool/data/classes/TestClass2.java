package org.kevoree.tool.data.classes;

import org.kevoree.tool.data.annotation.FieldAnnotation;

/**
 * Created by mleduc on 02/12/15.
 */
public class TestClass2 extends TestClass1 {
    @FieldAnnotation
    public int field4;

    private int notAnnotated1 = 3;
}
