package rest.servlet.core;

import jakarta.servlet.ServletConfig;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import kilpo.util.ClassScanner;
import rest.servlet.core.annotation.Component;
import rest.servlet.core.processor.*;
import rest.servlet.core.util.MappingMethod;

import java.lang.annotation.Annotation;
import java.lang.reflect.Parameter;
import java.util.Collection;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

public class Container {
    private final Map<Class<?>, Object> beans = new ConcurrentHashMap<>();
    private final Map<String, MappingMethod> mappingMethods = new ConcurrentHashMap<>();

    public Container(ServletConfig servletConfig) {
        List<Class<?>> classes = ClassScanner.scan();

        if (classes.isEmpty()) {
            throw new RuntimeException("no classes were found during container startup");
        }

        beans.put(servletConfig.getClass(), servletConfig);

        classes.stream()
                .filter(clazz -> !clazz.isAnnotation() &&
                        !clazz.isEnum() &&
                        !clazz.isAnnotation() &&
                        !clazz.isInterface() &&
                        isComponentPresent(clazz)
                )
                .forEach(clazz -> {
                    try {
                        beans.put(clazz, clazz.newInstance());
                    } catch (InstantiationException | IllegalAccessException e) {
                        throw new RuntimeException(e);
                    }
                });

        Collection<Object> beans = this.beans.values();
        AnnotationProcessor[] processors = new AnnotationProcessor[] {
                new PropertyAnnotationProcessor(),
                new ConfigurationAnnotationProcessor(),
                new InjectAnnotationProcessor(),
                new ControllerAnnotationProcessor()
        };

        for (AnnotationProcessor processor : processors) {
            beans.forEach(bean -> {
                try {
                    processor.processBean(bean, this);
                } catch (Exception e) {
                    e.printStackTrace();
                }
            });
        }
    }

    private boolean isComponentPresent(Class<?> clazz) {
        for (Annotation annotation : clazz.getDeclaredAnnotations()) {
            if (annotation.annotationType().isAnnotationPresent(Component.class)) {
                return true;
            }
        }

        return clazz.isAnnotationPresent(Component.class);
    }

    public Object getBean(Class<?> type) {
        return beans.get(type);
    }

    public void addBean(Class<?> type, Object bean) {
        beans.put(type, bean);
    }

    public void addMappingMethod(MappingMethod mappingMethod) {
        mappingMethods.put(mappingMethod.getUrl(), mappingMethod);
    }

    MappingMethod getMappingMethod(HttpServletRequest request, HttpServletResponse response) {
        MappingMethod mappingMethod = mappingMethods.get(request.getRequestURI());
        populateParameters(mappingMethod, request, response);
        return mappingMethod;
    }

    private void populateParameters(MappingMethod mappingMethod, HttpServletRequest request, HttpServletResponse response) {
        Parameter[] methodParameters = mappingMethod.getTargetMethod().getParameters();
        Object[] parameters = new Object[methodParameters.length];

        for (int a = 0; a < parameters.length; a++) {
            if (methodParameters[a].getType().equals(HttpServletRequest.class)) {
                parameters[a] = request;
            } else if (methodParameters[a].getType().equals(HttpServletResponse.class)) {
                parameters[a] = response;
            } else if (beans.containsKey(methodParameters[a].getType())) {
                parameters[a] = beans.get(methodParameters[a].getType());
            }
        }

        mappingMethod.setParameters(parameters);
    }
}
