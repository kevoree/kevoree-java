package org.kevoree.tool;

import org.junit.Assert;
import org.junit.Test;
import org.kevoree.tool.data.annotation.ClassAnnotation;
import org.kevoree.tool.data.annotation.FieldAnnotation;
import org.kevoree.tool.data.annotation.FieldAnnotation2;
import org.kevoree.tool.data.annotation.FieldAnnotation3;
import org.kevoree.tool.data.classes.TestClass1;
import org.kevoree.tool.data.classes.TestClass2;
import org.kevoree.tool.data.classes.TestClass3;

import java.lang.annotation.Annotation;
import java.lang.reflect.Field;
import java.util.List;

import static org.kevoree.tool.ReflectUtils.hasAnnotation;


/**
 * Created by mleduc on 02/12/15.
 */
public class ReflectUtilsTest {

    private final ReflectUtils reflectUtils = new ReflectUtils();

    @Test
    public void findAnnotationTest1() throws Exception {

        // FieldAnnotation should not be found because it is not on the class itself
        Assert.assertNull(ReflectUtils.findAnnotation(TestClass1.class, FieldAnnotation.class));
        Assert.assertFalse(hasAnnotation(TestClass1.class, FieldAnnotation.class));

        // ClassAnnotation should be found and should be a ClassAnnotation instance.
        assertExistsAndInstanceOf(ReflectUtils.findAnnotation(TestClass1.class, ClassAnnotation.class), ClassAnnotation.class);
        Assert.assertTrue(hasAnnotation(TestClass1.class, ClassAnnotation.class));
    }

    @Test
    public void findAnnotationTest2() throws Exception {
        // ClassAnnotation should be found because it is attached to the parent class of TestClass2
        final Annotation annotation = ReflectUtils.findAnnotation(TestClass2.class, ClassAnnotation.class);
        assertExistsAndInstanceOf(annotation, ClassAnnotation.class);
        Assert.assertTrue(hasAnnotation(TestClass2.class, ClassAnnotation.class));
    }

    @Test
    public void findAnnotationTest3() throws Exception {
        // ClassAnnotation should be found because it is attached to one of the implementations of Test2.
        final Annotation annotation = ReflectUtils.findAnnotation(TestClass3.class, ClassAnnotation.class);
        assertExistsAndInstanceOf(annotation, ClassAnnotation.class);
        Assert.assertTrue(hasAnnotation(TestClass3.class, ClassAnnotation.class));
    }

    private void assertExistsAndInstanceOf(Object annotation, Class<? extends Object> expectedClass) {
        Assert.assertNotNull(annotation);
        Assert.assertTrue(expectedClass.isInstance(annotation));
    }


    @Test
    public void findFieldWithAnnotation1() throws Exception {
        // should not be found because field1 is not annotated with ClassAnnotation
        Assert.assertNull(ReflectUtils.findFieldWithAnnotation("field1", TestClass1.class, ClassAnnotation.class));
        Assert.assertNull(ReflectUtils.getField("field3", TestClass1.class));

        // should be found because field1 is annotated with FieldAnnotation
        Assert.assertNotNull(ReflectUtils.findFieldWithAnnotation("field1", TestClass1.class, FieldAnnotation.class));
        Assert.assertNotNull(ReflectUtils.findFieldWithAnnotation("field2", TestClass1.class, FieldAnnotation.class));
        Assert.assertNotNull(ReflectUtils.findFieldWithAnnotation("field1", TestClass2.class, FieldAnnotation.class));
        Assert.assertNotNull(ReflectUtils.findFieldWithAnnotation("field2", TestClass2.class, FieldAnnotation.class));

        Assert.assertNotNull(ReflectUtils.getField("field1", TestClass1.class));
        Assert.assertNotNull(ReflectUtils.getField("field2", TestClass1.class));
        Assert.assertNotNull(ReflectUtils.getField("field1", TestClass2.class));
        Assert.assertNotNull(ReflectUtils.getField("field2", TestClass2.class));
    }


    @Test
    public void getAllFieldsWithAnnotation1() throws Exception {
        final List<Field> allFieldsWithAnnotation = ReflectUtils.getAllFieldsWithAnnotations(TestClass1.class, ClassAnnotation.class);
        Assert.assertNotNull(allFieldsWithAnnotation);
        Assert.assertEquals(0, allFieldsWithAnnotation.size());
    }

    @Test
    public void getAllFieldsWithAnnotation2() throws Exception {
        final List<Field> allFieldsWithAnnotation = ReflectUtils.getAllFieldsWithAnnotations(TestClass1.class, FieldAnnotation.class);
        Assert.assertNotNull(allFieldsWithAnnotation);
        Assert.assertEquals(2, allFieldsWithAnnotation.size());
    }

    @Test
    public void getAllFieldsWithAnnotation3() throws Exception {
        final List<Field> allFieldsWithAnnotation = ReflectUtils.getAllFieldsWithAnnotations(TestClass2.class, FieldAnnotation.class);
        Assert.assertNotNull(allFieldsWithAnnotation);
        Assert.assertEquals(3, allFieldsWithAnnotation.size());
    }

    @Test
    public void getAllFieldsWithAnnotations1() throws Exception {
        final List<Field> allFieldsWithAnnotations = ReflectUtils.getAllFieldsWithAnnotations(TestClass1.class, ClassAnnotation.class, FieldAnnotation3.class);
        Assert.assertNotNull(allFieldsWithAnnotations);
        Assert.assertEquals(0, allFieldsWithAnnotations.size());
    }

    @Test
    public void getAllFieldsWithAnnotations2() throws Exception {
        final List<Field> allFieldsWithAnnotations = ReflectUtils.getAllFieldsWithAnnotations(TestClass1.class, FieldAnnotation.class, FieldAnnotation2.class);
        Assert.assertNotNull(allFieldsWithAnnotations);
        Assert.assertEquals(3, allFieldsWithAnnotations.size());
    }

    @Test
    public void getAllFieldsWithAnnotations3() throws Exception {
        final List<Field> allFieldsWithAnnotations = ReflectUtils.getAllFieldsWithAnnotations(TestClass2.class, FieldAnnotation.class, FieldAnnotation2.class);
        Assert.assertNotNull(allFieldsWithAnnotations);
        Assert.assertEquals(4, allFieldsWithAnnotations.size());
    }

}
