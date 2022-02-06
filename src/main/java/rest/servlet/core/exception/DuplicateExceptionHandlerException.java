package rest.servlet.core.exception;

public class DuplicateExceptionHandlerException extends RuntimeException {
    public DuplicateExceptionHandlerException(Class<? extends Throwable> exceptionType, Class<?> clazz) {
        super(String.format("Duplicate exception handler for exception '%s' in class '%s'", exceptionType.getName(), clazz.getName()));
    }
}
