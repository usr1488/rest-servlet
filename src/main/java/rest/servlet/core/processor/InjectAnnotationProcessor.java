package rest.servlet.core.processor;

import rest.servlet.core.BeanContainer;
import rest.servlet.core.annotation.Inject;
import rest.servlet.core.exception.UnsatisfiedDependencyException;

import java.util.Arrays;
import java.util.Optional;

public class InjectAnnotationProcessor implements AnnotationProcessor {
    @Override
    public void processBean(Object component, BeanContainer beanContainer) {
        Arrays.stream(component.getClass().getDeclaredFields())
                .filter(field -> field.isAnnotationPresent(Inject.class))
                .forEach(field -> {
                    Object bean = Optional.ofNullable(beanContainer.getBean(field.getType()))
                            .orElseThrow(() -> new UnsatisfiedDependencyException(field.getType(), component.getClass()));

                    field.setAccessible(true);

                    try {
                        field.set(component, bean);
                    } catch (IllegalAccessException e) {
                        throw new RuntimeException(e);
                    }
                });
    }
}
