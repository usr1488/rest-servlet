package rest.servlet.core.exception;

import lombok.Getter;

@Getter
public class ContentTypeMismatchException extends RuntimeException {
    private final String contentType;
    private final String endpoint;

    public ContentTypeMismatchException(String contentType, String endpoint) {
        super(String.format("Content type '%s' not allowed on endpoint '%s'", contentType, endpoint));
        this.contentType = contentType;
        this.endpoint = endpoint;
    }
}
