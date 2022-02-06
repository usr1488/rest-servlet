package rest.servlet.core.processor;

import rest.servlet.core.BeanContainer;
import rest.servlet.core.annotation.Controller;
import rest.servlet.core.annotation.ExceptionHandler;
import rest.servlet.core.util.ExceptionHandlerMethod;

import java.lang.reflect.Method;

public class ExceptionHandlerAnnotationProcessor implements AnnotationProcessor {
    @Override
    public void processBean(Object bean, BeanContainer beanContainer) {
        if (!bean.getClass().isAnnotationPresent(Controller.class) && !bean.getClass().isAnnotationPresent(ExceptionHandler.class)) {
            return;
        }

        for (Method method : bean.getClass().getDeclaredMethods()) {
            if (!method.isAnnotationPresent(ExceptionHandler.class)) {
                continue;
            }

            method.setAccessible(true);
            beanContainer.addExceptionHandler(
                    ExceptionHandlerMethod.builder()
                            .targetObject(bean)
                            .targetMethod(method)
                            .exceptionType(method.getAnnotation(ExceptionHandler.class).value())
                            .build()
            );
        }
    }
}