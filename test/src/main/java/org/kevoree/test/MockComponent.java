package org.kevoree.test;

import java.lang.reflect.Constructor;
import java.lang.reflect.InvocationTargetException;

/**
 *
 * Created by leiko on 11/30/15.
 */
public class MockComponent {

    public static <T> T create(Class<T> clazz) {
        T comp = null;
        try {
            comp = clazz.newInstance();
        } catch (InstantiationException e) {
            e.printStackTrace();
        } catch (IllegalAccessException e) {
            System.err.println("Class "+clazz.getName()+" must have a public default constructor");
        }
        return comp;
    }
}
