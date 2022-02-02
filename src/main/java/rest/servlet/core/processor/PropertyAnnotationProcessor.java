package rest.servlet.core.processor;

import rest.servlet.core.Container;
import rest.servlet.core.annotation.Property;
import rest.servlet.core.util.PropertiesUtil;

import java.util.Arrays;

public class PropertyAnnotationProcessor implements AnnotationProcessor {
    @Override
    public void processBean(Object bean, Container container) {
        Arrays.stream(bean.getClass().getDeclaredFields())
                .filter(field -> field.isAnnotationPresent(Property.class))
                .forEach(field -> {
                    field.setAccessible(true);

                    try {
                        field.set(bean, PropertiesUtil.getProperty(field.getAnnotation(Property.class).value()));
                    } catch (IllegalAccessException e) {
                        throw new RuntimeException(e);
                    }
                });
    }
}