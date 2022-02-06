package rest.servlet.core.processor;

import rest.servlet.core.BeanContainer;

public interface AnnotationProcessor {
    void processBean(Object bean, BeanContainer beanContainer) throws Exception;
}
