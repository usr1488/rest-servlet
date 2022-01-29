package rest.servlet.type;

import lombok.Builder;
import lombok.Getter;
import lombok.Setter;
import rest.servlet.util.HttpMethod;

import java.lang.reflect.Method;

@Getter
@Setter
@Builder
public class MappingMethod {
    private Object targetObject;
    private Method targetMethod;
    private String url;
    private HttpMethod httpMethod;
    private Object[] parameters;
}
