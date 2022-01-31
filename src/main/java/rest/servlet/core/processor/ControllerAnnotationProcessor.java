package rest.servlet.core.processor;

import rest.servlet.core.Container;
import rest.servlet.core.annotation.Controller;
import rest.servlet.core.annotation.Mapping;
import rest.servlet.core.util.MappingMethod;

import java.lang.reflect.Method;

public class ControllerAnnotationProcessor implements AnnotationProcessor {
    @Override
    public void processComponent(Object component, Container container) {
        for (Method method : component.getClass().getDeclaredMethods()) {
            if (!method.isAnnotationPresent(Mapping.class)) {
                continue;
            }

            String controllerMapping = component.getClass().getAnnotation(Controller.class).value();
            String methodMapping = method.getAnnotation(Mapping.class).value();

            if (!controllerMapping.startsWith("/")) {
                controllerMapping = "/" + controllerMapping;
            }

            if (!methodMapping.startsWith("/")) {
                methodMapping = "/" + methodMapping;
            }

            method.setAccessible(true);
            container.addMappingMethod(
                    MappingMethod.builder()
                            .targetObject(component)
                            .targetMethod(method)
                            .url(controllerMapping.equals("/") ? "" + methodMapping : controllerMapping + methodMapping)
                            .httpMethod(method.getAnnotation(Mapping.class).method())
                            .build()
            );
        }
    }
}
