package rest.servlet.core.exception;

import lombok.Getter;
import rest.servlet.core.util.HttpMethod;

@Getter
public class MappingNotFoundException extends RuntimeException {
    private final String endpoint;
    private final HttpMethod method;

    public MappingNotFoundException(String endpoint, HttpMethod method) {
        super(String.format("No mapping found for '%s' endpoint with method '%s'", endpoint, method.name()));
        this.endpoint = endpoint;
        this.method = method;
    }
}
