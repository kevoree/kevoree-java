package org.kevoree.tool;


import java.lang.annotation.Annotation;
import java.lang.reflect.Field;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.stream.Collectors;

public class ReflectUtils {

    public static Annotation findAnnotation(Class<?> clazz, Class<? extends Annotation> annotationType) {
        Annotation a = clazz.getAnnotation(annotationType);
        if (a != null) {
            return a;
        }

        Class<?> parentClass = clazz.getSuperclass();
        if (parentClass != null && !parentClass.equals(Object.class)) {
            a = findAnnotation(parentClass, annotationType);
        }

        if (a == null) {
            for (Class<?> i : clazz.getInterfaces()) {
                a = findAnnotation(i, annotationType);
                if (a != null) {
                    return a;
                }
            }
        }

        return a;
    }

    public static Field findFieldWithAnnotation(String fieldName, Class<?> clazz, Class<? extends Annotation> annotationType) {
        Field field = getField(fieldName, clazz);
        if (field != null && field.isAnnotationPresent(annotationType)) {
            return field;
        }

        return null;
    }

    public static boolean hasAnnotation(Class<?> clazz, Class<? extends Annotation> annotationType) {
        return findAnnotation(clazz, annotationType) != null;
    }

    public static Field getField(String fieldName, Class<?> clazz) {
        List<Field> fields = getAllFields(clazz);
        for (Field field: fields) {
            if (field.getName().equals(fieldName)) {
                return field;
            }
        }
        return null;
    }

    public static List<Field> getAllFields(Class<?> clazz) {
        List<Field> currentClassFields = new ArrayList<>(Arrays.asList(clazz.getDeclaredFields()));
        Class<?> parentClass = clazz.getSuperclass();

        if (parentClass != null && !(parentClass.equals(Object.class))) {
            List<Field> parentClassFields = getAllFields(parentClass);
            currentClassFields.addAll(parentClassFields);
        }

        return currentClassFields;
    }

    public static List<Field> getAllFieldsWithAnnotation(Class<?> clazz, Class<? extends Annotation> annotationType) {
        return getAllFields(clazz)
                .stream()
                .filter(field -> field.isAnnotationPresent(annotationType))
                .collect(Collectors.toList());
    }

    public static List<Field> getAllFieldsWithAnnotations(Class<?> clazz, List<Class<? extends Annotation>> annotationTypes) {
        List<Field> fields = new ArrayList<>();
        for (Class<? extends Annotation> annotationType: annotationTypes) {
            fields.addAll(getAllFieldsWithAnnotation(clazz, annotationType));
        }
        return fields;
    }

    public static List<Field> getAllFieldsWithAnnotations(Class<?> clazz, Class<? extends Annotation>[] annotationTypes) {
        return getAllFieldsWithAnnotations(clazz, Arrays.asList(annotationTypes));
    }
}
