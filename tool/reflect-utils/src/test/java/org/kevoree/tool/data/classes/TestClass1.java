package org.kevoree.tool.data.classes;

import org.kevoree.tool.data.annotation.ClassAnnotation;
import org.kevoree.tool.data.annotation.FieldAnnotation;
import org.kevoree.tool.data.annotation.FieldAnnotation2;

/**
 * Created by mleduc on 02/12/15.
 */
@ClassAnnotation
public class TestClass1 {

    @FieldAnnotation
    private String field1;

    @FieldAnnotation
    public String field2;

    @FieldAnnotation2
    public String field21;

    private int notAnnotated2 = 1;
}
