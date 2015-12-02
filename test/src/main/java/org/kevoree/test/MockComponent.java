package org.kevoree.test;

import org.kevoree.annotations.Component;
import org.kevoree.annotations.Output;
import org.kevoree.annotations.params.BooleanParam;
import org.kevoree.annotations.params.IntParam;
import org.kevoree.annotations.params.StringParam;
import org.kevoree.test.exception.CreateMockException;
import org.kevoree.test.exception.SetFieldException;
import org.kevoree.tool.ReflectUtils;

import java.lang.annotation.Annotation;
import java.lang.reflect.Field;
import java.math.BigInteger;
import java.util.*;
import java.util.concurrent.ThreadLocalRandom;

/**
 *
 * Created by leiko on 11/30/15.
 */
public class MockComponent<T> {

    private List<Class<? extends Annotation>> paramAnnotations;
    private Class<T> clazz;
    private T instance;
    private Map<String, MockPort> ports = new HashMap<>();

    public MockComponent(Class<T> clazz) throws CreateMockException, SetFieldException {
        if (!ReflectUtils.hasAnnotation(clazz, Component.class)) {
            throw new CreateMockException("Class "+clazz.getName()+" is not a @Component");
        }

        this.clazz = clazz;
        this.paramAnnotations = new ArrayList<>();
        this.paramAnnotations.add(BooleanParam.class);
        this.paramAnnotations.add(StringParam.class);
        this.paramAnnotations.add(IntParam.class);

        try {
            instance = clazz.newInstance();
            generateParams();
            generatePorts();
        } catch (InstantiationException e) {
            throw new CreateMockException("Class "+clazz.getName()+" is not instantiable");
        } catch (IllegalAccessException e) {
            throw new CreateMockException("Class "+clazz.getName()+" must have a public default constructor");
        }
    }

    public MockComponent<T> setParam(String name, Object val) throws SetFieldException {
        Field field = ReflectUtils.getField(name, clazz);
        if (field != null) {
            if (field.isAnnotationPresent(IntParam.class) ||
                    field.isAnnotationPresent(BooleanParam.class) ||
                    field.isAnnotationPresent(StringParam.class)) {
                boolean isAccessible = field.isAccessible();
                if (!isAccessible) {
                    field.setAccessible(true);
                }
                try {
                    field.set(instance, val);
                } catch (IllegalAccessException e) {
                    throw new SetFieldException("Field "+field.getName()+" in class "+clazz.getName()+" cannot be set");
                } finally {
                    if (!isAccessible) {
                        field.setAccessible(false);
                    }
                }
            }
        } else {
            throw new SetFieldException("Field "+name+" does not exist in class "+clazz.getName());
        }
        return this;
    }

    public T get() {
        return instance;
    }

    @Override
    public String toString() {
        StringBuilder builder = new StringBuilder();
        builder.append(clazz.getName());
        builder.append(" {");

        for (Field field : ReflectUtils.getAllFieldsWithAnnotations(clazz, this.paramAnnotations)) {
            boolean isAccessible = field.isAccessible();
            if (!isAccessible) {
                field.setAccessible(true);
            }
            try {
                builder.append("\n");
                builder.append("  ");
                builder.append(field.getName());
                builder.append(" = ");
                builder.append(field.get(instance));
            } catch (IllegalAccessException e) {
                e.printStackTrace();
            } finally {
                if (!isAccessible) {
                    field.setAccessible(false);
                }
            }
        }

        builder.append("\n}");

        return builder.toString();
    }

    public MockPort expectPort(String name) {
        return this.ports.get(name);
    }

    public void verifyPorts() throws InterruptedException {
        for (MockPort port: this.ports.values()) {
            port.verify();
        }
    }

    private void generateParams() throws SetFieldException {
        for (Field field: ReflectUtils.getAllFields(clazz)) {
            boolean isAccessible = field.isAccessible();
            if (!isAccessible) {
                field.setAccessible(true);
            }
            try {
                Random random = new Random();
                if (field.isAnnotationPresent(IntParam.class)) {
                    IntParam anno = field.getAnnotation(IntParam.class);
                    int max = anno.max();
                    if (max == Integer.MAX_VALUE) {
                        max = Integer.MAX_VALUE - 1;
                    }
                    field.set(instance, ThreadLocalRandom.current().nextInt(anno.min(), max + 1));
                } else if (field.isAnnotationPresent(BooleanParam.class)) {
                    field.set(instance, random.nextBoolean());
                } else if (field.isAnnotationPresent(StringParam.class)) {
                    StringParam anno = field.getAnnotation(StringParam.class);
                    String str = new BigInteger(130, random).toString(32);
                    if (anno.multiline()) {
                        str = str.substring(0, 16) + "\n" + str.substring(15);
                    }
                    field.set(instance, str);
                }
            } catch (IllegalAccessException e) {
                throw new SetFieldException("Field "+field.getName()+" in class "+clazz.getName()+" cannot be set");
            } finally {
                if (!isAccessible) {
                    field.setAccessible(false);
                }
            }
        }
    }

    private void generatePorts() throws SetFieldException {
        for (Field field: ReflectUtils.getAllFieldsWithAnnotation(clazz, Output.class)) {
            boolean isAccessible = field.isAccessible();
            if (!isAccessible) {
                field.setAccessible(true);
            }
            try {
                MockPort mockPort = new MockPort(field.getName());
                ports.put(field.getName(), mockPort);
                field.set(instance, mockPort);
            } catch (IllegalAccessException e) {
                throw new SetFieldException("Field "+field.getName()+" in class "+clazz.getName()+" cannot be set");
            } finally {
                if (!isAccessible) {
                    field.setAccessible(false);
                }
            }
        }
    }
}