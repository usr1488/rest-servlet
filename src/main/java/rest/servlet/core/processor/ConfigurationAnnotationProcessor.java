package rest.servlet.core.processor;

import rest.servlet.core.Container;
import rest.servlet.core.annotation.Bean;
import rest.servlet.core.annotation.Configuration;

import java.lang.reflect.InvocationTargetException;
import java.util.Arrays;

public class ConfigurationAnnotationProcessor implements AnnotationProcessor {
    @Override
    public void processComponent(Object component, Container container) {
        if (!component.getClass().isAnnotationPresent(Configuration.class)) {
            return;
        }

        Arrays.stream(component.getClass().getDeclaredMethods())
                .filter(method -> method.isAnnotationPresent(Bean.class))
                .forEach(method -> {
                    try {
                        container.addBean(method.getReturnType(), method.invoke(component));
                    } catch (IllegalAccessException | InvocationTargetException e) {
                        throw new RuntimeException(e);
                    }
                });
    }
}
