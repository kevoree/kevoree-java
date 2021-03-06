package org.kevoree.tool;

import java.lang.annotation.Annotation;
import java.lang.reflect.Field;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class Injector {

    private Class<? extends Annotation> injectAnnotationClass;
    private Map<Class<?>, Object> registry = new HashMap<>();

    public Injector(Class<? extends Annotation> injectAnnotationClass) {
        this.injectAnnotationClass = injectAnnotationClass;
    }

    public <T> void register(Class<T> ctxType, T impl) {
        registry.put(ctxType, impl);
    }

    public void inject(Object instance) {
        List<Field> fields = ReflectUtils.getAllFieldsWithAnnotation(instance.getClass(), injectAnnotationClass);
        for (Field field : fields) {
            Object impl = this.registry.get(field.getType());
            if (impl != null) {
                boolean isAccessible = field.isAccessible();
                if (!isAccessible) {
                    field.setAccessible(true);
                }
                try {
                    field.set(instance, impl);
                } catch (IllegalAccessException e) {
                    e.printStackTrace();
                } finally {
                    if (!isAccessible) {
                        field.setAccessible(false);
                    }
                }
            } else {
                throw new Error("Unable to find implementation of type "+field.getType().getName()+" to inject.");
            }
        }
    }
}