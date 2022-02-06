package rest.servlet.core.exception;

public class DuplicateEndpointException extends RuntimeException {
    public DuplicateEndpointException(String endpoint, Class<?> clazz) {
        super(String.format("Duplicated endpoint '%s' in controller '%s'", endpoint, clazz.getName()));
    }
}
