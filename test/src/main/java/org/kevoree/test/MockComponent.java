package org.kevoree.test;

import org.kevoree.annotations.Component;
import org.kevoree.annotations.Output;
import org.kevoree.annotations.params.*;
import org.kevoree.test.exception.CreateMockException;
import org.kevoree.test.exception.SetFieldException;
import org.kevoree.tool.ReflectUtils;

import java.lang.annotation.Annotation;
import java.lang.reflect.Field;
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
        this.paramAnnotations.add(Param.class);

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
            if (field.isAnnotationPresent(Param.class)) {
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
                Number min = null;
                if (field.isAnnotationPresent(Min.class)) {
                    min = field.getAnnotation(Min.class).value();
                }
                Number max = null;
                if (field.isAnnotationPresent(Max.class)) {
                    max = field.getAnnotation(Max.class).value();
                }
                Integer length = null;
                if (field.isAnnotationPresent(Length.class)) {
                    length = field.getAnnotation(Length.class).value();
                }

                if (field.getType().equals(Integer.class)) {
                    min = min == null ? Integer.MIN_VALUE : min;
                    max = max == null ? Integer.MAX_VALUE : max;
                    if (max.intValue() == Integer.MAX_VALUE) {
                        max = Integer.MAX_VALUE - 1;
                    }
                    field.set(instance, ThreadLocalRandom.current().nextInt(min.intValue(), max.intValue() + 1));

                } else if (field.getType().equals(Long.class)) {
                    min = min == null ? Long.MIN_VALUE : min;
                    max = max == null ? Long.MAX_VALUE : max;
                    if (max.longValue() == Long.MAX_VALUE) {
                        max = Long.MAX_VALUE - 1;
                    }
                    field.set(instance, ThreadLocalRandom.current().nextLong(min.longValue(), max.longValue() + 1));

                } else if (field.getType().equals(Short.class)) {
                    min = min == null ? Short.MIN_VALUE : min;
                    max = max == null ? Short.MAX_VALUE : max;
                    if (max.shortValue() == Short.MAX_VALUE) {
                        max = Short.MAX_VALUE - 1;
                    }
                    field.set(instance, (short) ThreadLocalRandom.current().nextInt(min.shortValue(), max.shortValue() + 1));

                } else if (field.getType().equals(Double.class)) {
                    min = min == null ? Double.MIN_VALUE : min;
                    max = max == null ? Double.MAX_VALUE : max;
                    if (max.doubleValue() == Double.MAX_VALUE) {
                        max = Double.MAX_VALUE - 1;
                    }
                    field.set(instance, ThreadLocalRandom.current().nextDouble(min.doubleValue(), max.doubleValue() + 1));

                } else if (field.getType().equals(Float.class)) {
                    min = min == null ? Float.MIN_VALUE : min;
                    max = max == null ? Float.MAX_VALUE : max;
                    if (max.floatValue() == Float.MAX_VALUE) {
                        max = Float.MAX_VALUE - 1;
                    }
                    field.set(instance, ThreadLocalRandom.current().nextFloat() * (max.floatValue() - min.floatValue()) + min.floatValue());

                } else if (field.getType().equals(Boolean.class)) {
                    field.set(instance, random.nextBoolean());

                } else if (field.getType().equals(String.class)) {
                    String str = "";
                    if (length != null) {
                        str = randomStr(0, length);
                    } else {
                        min = min == null ? random.nextInt(100) : min;
                        max = max == null ? random.nextInt(250) : max;
                        str = randomStr(min.intValue(), max.intValue());
                    }

                    if (field.isAnnotationPresent(Multiline.class)) {
                        str = str.substring(0, str.length()/2) + "\n" + str.substring((str.length()/2));
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

    private String randomStr(int min, int max) {
        char[] chars = "abcdefghijklmnopqrstuvwxyzABCDEFGHIJKLMNOPQRSTUVWXYZ0123456789 &é\"'(-è_çà)=#~²{[|`\\^@]}^$ù*¨£%µ!§:/;.,?<>*-+ø".toCharArray();
        StringBuilder sb = new StringBuilder();
        Random random = new Random();
        for (int i = 0; i < (max - min); i++) {
            char c = chars[random.nextInt(chars.length)];
            sb.append(c);
        }
        return sb.toString();
    }
}
