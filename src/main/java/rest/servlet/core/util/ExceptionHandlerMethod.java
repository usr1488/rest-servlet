package rest.servlet.core.util;

import lombok.Builder;
import lombok.Getter;
import lombok.Setter;

import java.lang.reflect.Method;

@Getter
@Setter
@Builder
public class ExceptionHandlerMethod {
    private volatile Object targetObject;
    private volatile Method targetMethod;
    private volatile Object[] methodArguments;
    private volatile Class<? extends Throwable> exceptionType;
}
