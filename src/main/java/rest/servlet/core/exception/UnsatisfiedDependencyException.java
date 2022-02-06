package rest.servlet.core.exception;

public class UnsatisfiedDependencyException extends RuntimeException {
    public UnsatisfiedDependencyException(Class<?> beanType, Class<?> componentType) {
        super(String.format("Bean with type '%s' not found for component '%s'", beanType.getName(), componentType.getName()));
    }
}
