package rest.servlet.core.processor;

import rest.servlet.core.BeanContainer;
import rest.servlet.core.annotation.Controller;
import rest.servlet.core.annotation.Mapping;
import rest.servlet.core.exception.InvalidReturnTypeException;
import rest.servlet.core.util.MappingMethod;

import java.lang.reflect.Method;

public class ControllerAnnotationProcessor implements AnnotationProcessor {
    @Override
    public void processBean(Object bean, BeanContainer beanContainer) {
        if (!bean.getClass().isAnnotationPresent(Controller.class)) {
            return;
        }

        for (Method method : bean.getClass().getDeclaredMethods()) {
            if (!method.isAnnotationPresent(Mapping.class)) {
                continue;
            }

            Mapping mapping = method.getAnnotation(Mapping.class);
            String controllerMapping = bean.getClass().getAnnotation(Controller.class).value();
            String methodMapping = mapping.value();

            if (!controllerMapping.startsWith("/")) {
                controllerMapping = "/" + controllerMapping;
            }

            if (!methodMapping.startsWith("/")) {
                methodMapping = "/" + methodMapping;
            }

            if (mapping.isPage() && !method.getReturnType().equals(String.class)) {
                throw new InvalidReturnTypeException(method, bean.getClass());
            }

            method.setAccessible(true);
            beanContainer.addMappingMethod(
                    MappingMethod.builder()
                            .targetObject(bean)
                            .targetMethod(method)
                            .url(controllerMapping.equals("/") ? "" + methodMapping : controllerMapping + methodMapping)
                            .httpMethod(mapping.method())
                            .acceptContentType(mapping.acceptContentType().trim())
                            .isPage(mapping.isPage())
                            .build()
            );
        }
    }
}
