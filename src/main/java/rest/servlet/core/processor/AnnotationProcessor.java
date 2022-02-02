package rest.servlet.core.processor;

import rest.servlet.core.Container;

public interface AnnotationProcessor {
    void processBean(Object bean, Container container) throws Exception;
}
