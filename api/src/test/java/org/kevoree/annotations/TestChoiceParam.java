package org.kevoree.annotations;

import org.junit.Test;
import org.kevoree.annotations.params.Param;
import org.kevoree.comp.FakeComp;
import org.kevoree.tool.ReflectUtils;

import java.lang.reflect.Field;

/**
 *
 * Created by leiko on 12/2/15.
 */
public class TestChoiceParam {

    private FakeComp comp = new FakeComp();

    @Test
    public void testDefault() throws IllegalAccessException {
        Field field = ReflectUtils.findFieldWithAnnotation("myEnum", FakeComp.class, Param.class);
        if (field != null) {
            field.setAccessible(true);
            Object o = field.get(comp);
            if (o instanceof Enum) {
                Object[] enumConstants = o.getClass().getEnumConstants();
                for (Object constant: enumConstants) {
                    System.out.println(constant);
                }
            }
            field.setAccessible(false);
        }
    }
}
