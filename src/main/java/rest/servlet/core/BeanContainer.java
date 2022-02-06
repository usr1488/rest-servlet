package rest.servlet.core;

import jakarta.servlet.ServletConfig;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import kilpo.util.ClassScanner;
import rest.servlet.core.annotation.Component;
import rest.servlet.core.annotation.ExceptionHandler;
import rest.servlet.core.annotation.RequestBody;
import rest.servlet.core.converter.RequestBodyConverter;
import rest.servlet.core.exception.DuplicateBeanException;
import rest.servlet.core.exception.DuplicateEndpointException;
import rest.servlet.core.exception.DuplicateExceptionHandlerException;
import rest.servlet.core.exception.UnsupportedContentTypeException;
import rest.servlet.core.processor.*;
import rest.servlet.core.util.ExceptionHandlerMethod;
import rest.servlet.core.util.MappingMethod;

import java.io.IOException;
import java.lang.annotation.Annotation;
import java.lang.reflect.Parameter;
import java.util.Collection;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.CopyOnWriteArrayList;

public class BeanContainer {
    private final Map<Class<?>, Object> beans = new ConcurrentHashMap<>();
    private final List<MappingMethod> mappingMethods = new CopyOnWriteArrayList<>();
    private final List<ExceptionHandlerMethod> exceptionHandlers = new CopyOnWriteArrayList<>();
    private final List<RequestBodyConverter> requestBodyConverters = new CopyOnWriteArrayList<>();

    BeanContainer(ServletConfig servletConfig) {
        List<Class<?>> classes = ClassScanner.scan();

        if (classes.isEmpty()) {
            throw new RuntimeException("no classes were found during container startup");
        }

        beans.put(servletConfig.getClass(), servletConfig);

        classes.stream()
                .filter(clazz -> RequestBodyConverter.class.isAssignableFrom(clazz) &&
                        !clazz.isInterface() &&
                        !clazz.isEnum() &&
                        !clazz.isAnnotation()
                )
                .map(clazz -> {
                    try {
                        return (RequestBodyConverter) clazz.newInstance();
                    } catch (InstantiationException | IllegalAccessException e) {
                        throw new RuntimeException(e);
                    }
                })
                .forEach(requestBodyConverters::add);

        classes.stream()
                .filter(clazz -> isComponentPresent(clazz) &&
                        !clazz.isInterface() &&
                        !clazz.isEnum() &&
                        !clazz.isAnnotation()
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
                new ExceptionHandlerAnnotationProcessor(),
                new ControllerAnnotationProcessor()
        };

        for (AnnotationProcessor processor : processors) {
            beans.forEach(bean -> {
                try {
                    processor.processBean(bean, this);
                } catch (Exception e) {
                    throw new RuntimeException(e);
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
        beans.merge(type, bean, (oldValue, newValue) -> {
            throw new DuplicateBeanException(type);
        });
    }

    MappingMethod getMappingMethod(HttpServletRequest request) {
        return mappingMethods.stream()
                .filter(mappingMethod ->
                        mappingMethod.getUrl().equals(request.getRequestURI()) &&
                                mappingMethod.getHttpMethod().name().equals(request.getMethod())
                )
                .findFirst()
                .orElse(null);
    }

    public void addMappingMethod(MappingMethod newMappingMethod) {
        boolean exist = mappingMethods.stream().anyMatch(mappingMethod ->
                mappingMethod.getUrl().equals(newMappingMethod.getUrl()) &&
                        mappingMethod.getHttpMethod().equals(newMappingMethod.getHttpMethod())
        );

        if (exist) {
            throw new DuplicateEndpointException(newMappingMethod.getUrl(), newMappingMethod.getTargetObject().getClass());
        }

        mappingMethods.add(newMappingMethod);
    }

    ExceptionHandlerMethod getExceptionHandler(MappingMethod mappingMethod, Throwable throwable, HttpServletRequest request, HttpServletResponse response) {
        ExceptionHandlerMethod handler = null;

        // search for exception handler in controllers
        if (mappingMethod != null) {
            handler = exceptionHandlers.stream()
                    .filter(exceptionHandlerMethod ->
                            exceptionHandlerMethod.getTargetObject().getClass().equals(mappingMethod.getTargetObject().getClass()) &&
                                    exceptionHandlerMethod.getExceptionType().isAssignableFrom(throwable.getClass())
                    )
                    .findFirst()
                    .orElse(null);
        }

        // search for global exception handlers
        if (handler == null) {
            handler = exceptionHandlers.stream()
                    .filter(exceptionHandlerMethod ->
                            exceptionHandlerMethod.getTargetObject().getClass().isAnnotationPresent(ExceptionHandler.class) &&
                                    exceptionHandlerMethod.getExceptionType().isAssignableFrom(throwable.getClass())
                    )
                    .findFirst()
                    .orElseGet(() -> {
                        response.setStatus(500);
                        throwable.printStackTrace();
                        throw new RuntimeException(throwable);
                    });
        }

        handler.getTargetMethod().setAccessible(true);
        populateExceptionHandlerArguments(handler, request, response, throwable);

        return handler;
    }

    public void addExceptionHandler(ExceptionHandlerMethod newExceptionHandlerMethod) {
        boolean exist = exceptionHandlers.stream().anyMatch(exceptionHandlerMethod ->
                exceptionHandlerMethod.getTargetObject().getClass().equals(newExceptionHandlerMethod.getTargetObject().getClass()) &&
                        exceptionHandlerMethod.getExceptionType().equals(newExceptionHandlerMethod.getExceptionType())
                        ||
                        exceptionHandlerMethod.getTargetObject().getClass().isAnnotationPresent(ExceptionHandler.class) &&
                                newExceptionHandlerMethod.getTargetObject().getClass().isAnnotationPresent(ExceptionHandler.class) &&
                                exceptionHandlerMethod.getExceptionType().equals(newExceptionHandlerMethod.getExceptionType())
                        ||
                        exceptionHandlerMethod.getExceptionType().equals(newExceptionHandlerMethod.getExceptionType())
        );

        if (exist) {
            throw new DuplicateExceptionHandlerException(
                    newExceptionHandlerMethod.getExceptionType(),
                    newExceptionHandlerMethod.getTargetObject().getClass()
            );
        }

        exceptionHandlers.add(newExceptionHandlerMethod);
    }

    void populateMappingMethodArguments(MappingMethod mappingMethod, HttpServletRequest request, HttpServletResponse response) {
        Parameter[] targetMethodArguments = mappingMethod.getTargetMethod().getParameters();
        Object[] arguments = new Object[targetMethodArguments.length];

        for (int a = 0; a < arguments.length; a++) {
            if (targetMethodArguments[a].getType().equals(HttpServletRequest.class)) {
                arguments[a] = request;
            } else if (targetMethodArguments[a].getType().equals(HttpServletResponse.class)) {
                arguments[a] = response;
            } else if (targetMethodArguments[a].isAnnotationPresent(RequestBody.class)) {
                arguments[a] = convertRequestBody(request, targetMethodArguments[a].getType());
            } else if (beans.containsKey(targetMethodArguments[a].getType())) {
                arguments[a] = beans.get(targetMethodArguments[a].getType());
            }
        }

        mappingMethod.setMethodArguments(arguments);
    }

    private <T> T convertRequestBody(HttpServletRequest request, Class<T> type) {
        return requestBodyConverters.stream()
                .filter(requestBodyConverter -> requestBodyConverter.supportedContentTypes().contains(request.getContentType()))
                .findFirst()
                .map(requestBodyConverter -> {
                    try {
                        return requestBodyConverter.convert(request.getInputStream(), type);
                    } catch (IOException e) {
                        throw new RuntimeException(e);
                    }
                })
                .orElseThrow(() -> new UnsupportedContentTypeException(request.getContentType(), request.getRequestURI()));
    }

    private void populateExceptionHandlerArguments(ExceptionHandlerMethod exceptionHandlerMethod, HttpServletRequest request, HttpServletResponse response, Throwable throwable) {
        Parameter[] targetMethodArguments = exceptionHandlerMethod.getTargetMethod().getParameters();
        Object[] arguments = new Object[targetMethodArguments.length];

        for (int a = 0; a < arguments.length; a++) {
            if (targetMethodArguments[a].getType().isAssignableFrom(HttpServletRequest.class)) {
                arguments[a] = request;
            } else if (targetMethodArguments[a].getType().isAssignableFrom(HttpServletResponse.class)) {
                arguments[a] = response;
            } else if (targetMethodArguments[a].getType().isAssignableFrom(throwable.getClass())) {
                arguments[a] = throwable;
            } else if (beans.containsKey(targetMethodArguments[a].getType())) {
                arguments[a] = beans.get(targetMethodArguments[a].getType());
            }
        }

        exceptionHandlerMethod.setMethodArguments(arguments);
    }
}
