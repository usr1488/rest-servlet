package rest.servlet.core;

import com.fasterxml.jackson.databind.ObjectMapper;
import jakarta.servlet.ServletConfig;
import jakarta.servlet.ServletException;
import jakarta.servlet.annotation.WebServlet;
import jakarta.servlet.http.HttpServlet;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import rest.servlet.core.util.MappingMethod;

import java.io.IOException;
import java.lang.reflect.InvocationTargetException;

@WebServlet("/")
public class DispatcherServlet extends HttpServlet {
    private static final ObjectMapper OBJECT_MAPPER = new ObjectMapper();
    private volatile Container container;

    @Override
    public void init(ServletConfig servletConfig) throws ServletException {
        container = new Container(servletConfig);
        super.init(servletConfig);
    }

    @Override
    protected void service(HttpServletRequest request, HttpServletResponse response) throws IOException {
        MappingMethod mappingMethod = container.getMappingMethod(request, response);

        if (mappingMethod == null) {
            response.getWriter().write(String.format("endpoint '%s' not found", request.getRequestURI()));
        } else if (!mappingMethod.getHttpMethod().name().equals(request.getMethod())) {
            response.getWriter().write(String.format("method '%s' not allowed on this endpoint", request.getMethod()));
        } else {
            try {
                Object result = mappingMethod.getTargetMethod().invoke(mappingMethod.getTargetObject(), mappingMethod.getParameters());

                if (result != null && result.getClass().equals(String.class)) {
                    response.setContentType("text/plain");
                    response.getWriter().write((String) result);
                } else if (result != null) {
                    response.setContentType("application/json");
                    response.getWriter().write(OBJECT_MAPPER.writeValueAsString(result));
                }
            } catch (IllegalAccessException | InvocationTargetException | IOException e) {
                throw new RuntimeException(e);
            }
        }
    }
}
