package rest.servlet.core.exception;

import lombok.Getter;

@Getter
public class UnsupportedContentTypeException extends RuntimeException {
    private final String contentType;
    private final String endpoint;

    public UnsupportedContentTypeException(String contentType, String endpoint) {
        super(String.format("Unable to convert request body with content type '%s' on endpoint '%s'", contentType, endpoint));
        this.contentType = contentType;
        this.endpoint = endpoint;
    }
}
