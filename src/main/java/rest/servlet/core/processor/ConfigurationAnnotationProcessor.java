package rest.servlet.core.processor;

import rest.servlet.core.Container;
import rest.servlet.core.annotation.Bean;
import rest.servlet.core.annotation.Configuration;

import java.lang.reflect.InvocationTargetException;
import java.util.Arrays;

public class ConfigurationAnnotationProcessor implements AnnotationProcessor {
    @Override
    public void processBean(Object bean, Container container) {
        if (!bean.getClass().isAnnotationPresent(Configuration.class)) {
            return;
        }

        Arrays.stream(bean.getClass().getDeclaredMethods())
                .filter(method -> method.isAnnotationPresent(Bean.class))
                .forEach(method -> {
                    method.setAccessible(true);

                    try {
                        container.addBean(method.getReturnType(), method.invoke(bean));
                    } catch (IllegalAccessException | InvocationTargetException e) {
                        throw new RuntimeException(e);
                    }
                });
    }
}
