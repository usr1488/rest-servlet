package rest.servlet.core.exception;

public class DuplicateBeanException extends RuntimeException {
    public DuplicateBeanException(Class<?> beanType) {
        super(String.format("Found duplicate bean with type '%s'", beanType.getName()));
    }
}
