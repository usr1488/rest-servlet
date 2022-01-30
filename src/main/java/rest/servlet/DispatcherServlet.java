package rest.servlet;

import com.fasterxml.jackson.databind.ObjectMapper;
import jakarta.servlet.ServletConfig;
import jakarta.servlet.ServletException;
import jakarta.servlet.annotation.WebServlet;
import jakarta.servlet.http.HttpServlet;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import kilpo.util.ClassScanner;
import rest.servlet.annotation.Controller;
import rest.servlet.annotation.Mapping;
import rest.servlet.type.MappingMethod;

import java.io.IOException;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.lang.reflect.Parameter;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

@WebServlet("/")
public class DispatcherServlet extends HttpServlet {
    private static final Map<String, MappingMethod> CONTROLLER_MAP = new ConcurrentHashMap<>();
    private static final ObjectMapper OBJECT_MAPPER = new ObjectMapper();

    @Override
    public void init(ServletConfig servletConfig) throws ServletException {
        super.init(servletConfig);

        List<Class<?>> scan = ClassScanner.scan();

        if (scan.isEmpty()) {
            throw new RuntimeException("0 classes was found during DispatcherServlet initialization");
        }

        scan.stream()
                .filter(clazz -> clazz.isAnnotationPresent(Controller.class))
                .map(clazz -> {
                    List<MappingMethod> mappingMethods = new ArrayList<>();

                    try {
                        for (Method method : clazz.getDeclaredMethods()) {
                            if (method.isAnnotationPresent(Mapping.class)) {
                                String controllerMapping = clazz.getAnnotation(Controller.class).value();
                                String methodMapping = method.getAnnotation(Mapping.class).value();

                                if (controllerMapping.isEmpty() || !controllerMapping.startsWith("/")) {
                                    controllerMapping = "/" + controllerMapping;
                                }

                                if (methodMapping.isEmpty() || !methodMapping.startsWith("/")) {
                                    methodMapping = "/" + methodMapping;
                                }

                                mappingMethods.add(
                                        MappingMethod.builder()
                                                .targetObject(clazz.newInstance())
                                                .targetMethod(method)
                                                .url(controllerMapping.equals("/") ? "" + methodMapping : controllerMapping + methodMapping)
                                                .httpMethod(method.getAnnotation(Mapping.class).method())
                                                .build()
                                );
                            }
                        }
                    } catch (InstantiationException | IllegalAccessException e) {
                        throw new RuntimeException(e);
                    }

                    return mappingMethods;
                })
                .flatMap(Collection::stream)
                .peek(mappingMethod -> {
                    Parameter[] methodParameters = mappingMethod.getTargetMethod().getParameters();
                    Object[] parameters = new Object[methodParameters.length];

                    for (int a = 0; a < parameters.length; a++) {
                        if (methodParameters[a].getType().equals(ServletConfig.class)) {
                            parameters[a] = servletConfig;
                        }
                    }

                    mappingMethod.setParameters(parameters);
                })
                .forEach(mappingMethod -> CONTROLLER_MAP.putIfAbsent(mappingMethod.getUrl(), mappingMethod));
    }

    @Override
    protected void service(HttpServletRequest request, HttpServletResponse response) throws IOException {
        MappingMethod mappingMethod = CONTROLLER_MAP.get(request.getRequestURI());

        if (mappingMethod == null) {
            response.getWriter().write(String.format("endpoint '%s' not found", request.getRequestURI()));
        } else if (!mappingMethod.getHttpMethod().name().equals(request.getMethod())) {
            response.getWriter().write(String.format("method '%s' not allowed on this endpoint", request.getMethod()));
        } else {
            try {
                Object result = mappingMethod.getTargetMethod().invoke(
                        mappingMethod.getTargetObject(),
                        parseMethodArguments(mappingMethod, request, response)
                );

                if (result.getClass().equals(String.class)) {
                    response.setContentType("text/plain");
                    response.getWriter().write((String) result);
                } else if (!result.getClass().equals(Void.class)) {
                    response.setContentType("application/json");
                    response.getWriter().write(OBJECT_MAPPER.writeValueAsString(result));
                }
            } catch (IllegalAccessException | InvocationTargetException | IOException e) {
                throw new RuntimeException(e);
            }
        }
    }

    private Object[] parseMethodArguments(MappingMethod mappingMethod, HttpServletRequest request, HttpServletResponse response) {
        Parameter[] methodParameters = mappingMethod.getTargetMethod().getParameters();
        Object[] parameters = mappingMethod.getParameters();

        for (int a = 0; a < parameters.length; a++) {
            if (methodParameters[a].getType().equals(HttpServletRequest.class)) {
                parameters[a] = request;
            } else if (methodParameters[a].getType().equals(HttpServletResponse.class)) {
                parameters[a] = response;
            }
        }

        return parameters;
    }
}
