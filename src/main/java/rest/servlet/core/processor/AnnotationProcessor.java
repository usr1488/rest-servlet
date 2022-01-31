package rest.servlet.core.processor;

import rest.servlet.core.Container;

public interface AnnotationProcessor {
    void processComponent(Object component, Container container) throws Exception;
}
