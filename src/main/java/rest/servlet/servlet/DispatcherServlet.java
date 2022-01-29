package rest.servlet.servlet;

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
import java.lang.reflect.Parameter;
import java.util.Arrays;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

@WebServlet("/")
public class DispatcherServlet extends HttpServlet {
    private static final NotFoundController DEFAULT_CONTROLLER = new NotFoundController();
    private static final Map<String, Object> CONTROLLER_MAP = new ConcurrentHashMap<>();

    @Override
    public void init() throws ServletException {
        List<Class<?>> scan = ClassScanner.scan();

        if (scan.isEmpty()) {
            throw new RuntimeException("0 classes was found");
        }

        scan.stream()
                .filter(clazz -> clazz.isAnnotationPresent(Controller.class))
                .forEach(clazz -> {
                    try {
                        CONTROLLER_MAP.putIfAbsent(clazz.getAnnotation(Controller.class).value(), clazz.newInstance());
                    } catch (InstantiationException | IllegalAccessException e) {
                        throw new RuntimeException(e);
                    }
                });

        super.init();
    }

    @Override
    protected void service(HttpServletRequest request, HttpServletResponse response) throws IOException {
        Object o = CONTROLLER_MAP.get(request.getRequestURI());

        if (o == null) {
            DEFAULT_CONTROLLER.handle(request, response);
        } else {
            Arrays.stream(o.getClass().getDeclaredMethods())
                    .filter(method -> method.isAnnotationPresent(Mapping.class))
                    .map(method -> {
                        Parameter[] methodParameters = method.getParameters();
                        Object[] parameters = new Object[methodParameters.length];

                        for (int a = 0; a < parameters.length; a++) {
                            if (methodParameters[a].getType().equals(HttpServletRequest.class)) {
                                parameters[a] = request;
                            } else if (methodParameters[a].getType().equals(HttpServletResponse.class)) {
                                parameters[a] = response;
                            }
                        }

                        return MappingMethod.builder()
                                .method(method)
                                .url(method.getAnnotation(Mapping.class).value())
                                .httpMethod(method.getAnnotation(Mapping.class).method())
                                .parameters(parameters)
                                .build();
                    })
                    .filter(mappingMethod -> mappingMethod.getHttpMethod().name().equals(request.getMethod()) && request.getRequestURI().endsWith(mappingMethod.getUrl()))
                    .forEach(mappingMethod -> {
                        try {
                            Object result = mappingMethod.getMethod().invoke(o, mappingMethod.getParameters());

                            if (result.getClass().equals(String.class)) {
                                response.getWriter().write((String) result);
                            }
                        } catch (IllegalAccessException | InvocationTargetException | IOException e) {
                            e.printStackTrace();
                        }
                    });
        }
    }

    private static final class NotFoundController {
        public void handle(HttpServletRequest request, HttpServletResponse response) throws IOException {
            response.getWriter().write(String.format("endpoint '%s' not found", request.getRequestURI()));
        }
    }
}
