package rest.servlet.core.exception;

import java.lang.reflect.Method;

public class InvalidReturnTypeException extends RuntimeException {
    public InvalidReturnTypeException(Method method, Class<?> controllerClass) {
        super(String.format("Expected return type for method '%s' in controller '%s' is 'java.lang.String'", method.getName(), controllerClass.getName()));
    }
}
