package rest.servlet.core.exception;

import lombok.Getter;
import rest.servlet.core.util.HttpMethod;

@Getter
public class MethodNotAllowedException extends RuntimeException {
    private final HttpMethod method;
    private final String endpoint;

    public MethodNotAllowedException(HttpMethod method, String endpoint) {
        super(String.format("Method '%s' not allowed on '%s' endpoint", method.name(), endpoint));
        this.method = method;
        this.endpoint = endpoint;
    }
}
