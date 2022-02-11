package rest.servlet.core;

import com.fasterxml.jackson.databind.ObjectMapper;
import jakarta.servlet.ServletConfig;
import jakarta.servlet.ServletException;
import jakarta.servlet.annotation.WebServlet;
import jakarta.servlet.http.HttpServlet;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import rest.servlet.core.exception.ContentTypeMismatchException;
import rest.servlet.core.exception.MappingNotFoundException;
import rest.servlet.core.exception.MethodNotAllowedException;
import rest.servlet.core.util.ExceptionHandlerMethod;
import rest.servlet.core.util.HttpMethod;
import rest.servlet.core.util.MappingMethod;

import java.io.IOException;
import java.io.InputStream;
import java.lang.reflect.InvocationTargetException;
import java.util.Optional;

@WebServlet("/")
public class DispatcherServlet extends HttpServlet {
    private final ObjectMapper objectMapper = new ObjectMapper();
    private volatile BeanContainer beanContainer;

    @Override
    public void init(ServletConfig servletConfig) throws ServletException {
        super.init(servletConfig);
        beanContainer = (BeanContainer) servletConfig.getServletContext().getAttribute("beanContainer");
    }

    @Override
    protected void service(HttpServletRequest request, HttpServletResponse response) throws IOException, ServletException {
        MappingMethod mappingMethod = null;
        Object result;
        boolean isPage;

        try {
            mappingMethod = beanContainer.getMappingMethod(request);
            result = handleRequest(mappingMethod, request, response);
            isPage = mappingMethod != null && mappingMethod.isPage();
        } catch (Throwable throwable) {
            ExceptionHandlerMethod exceptionHandlerMethod = beanContainer.getExceptionHandler(
                    mappingMethod,
                    Optional.ofNullable(throwable.getCause()).orElse(throwable),
                    request,
                    response
            );

            try {
                result = exceptionHandlerMethod.getTargetMethod().invoke(exceptionHandlerMethod.getTargetObject(), exceptionHandlerMethod.getMethodArguments());
            } catch (IllegalAccessException | InvocationTargetException e) {
                throw new RuntimeException(e);
            }

            isPage = exceptionHandlerMethod.isPage();
        }

        handleResult(result, isPage, request, response);
    }

    private Object handleRequest(MappingMethod mappingMethod, HttpServletRequest request, HttpServletResponse response) throws Throwable {
        if (mappingMethod == null) {
            InputStream inputStream = request.getServletContext().getResourceAsStream(request.getRequestURI());

            if (inputStream == null) {
                throw new MappingNotFoundException(request.getRequestURI(), HttpMethod.valueOf(request.getMethod()));
            }

            byte[] buffer = new byte[4096];
            int count;

            while ((count = inputStream.read(buffer)) != -1) {
                response.getOutputStream().write(buffer, 0, count);
            }

            return null;
        } else if (!mappingMethod.getHttpMethod().name().equals(request.getMethod())) {
            throw new MethodNotAllowedException(mappingMethod.getHttpMethod(), mappingMethod.getUrl());
        } else if (!mappingMethod.getAcceptContentType().isEmpty() && !mappingMethod.getAcceptContentType().equals(request.getContentType())) {
            throw new ContentTypeMismatchException(request.getContentType(), mappingMethod.getUrl());
        } else {
            beanContainer.populateMappingMethodArguments(mappingMethod, request, response);
            return mappingMethod.getTargetMethod().invoke(mappingMethod.getTargetObject(), mappingMethod.getMethodArguments());
        }
    }

    private void handleResult(Object result, boolean isPage, HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
        if (result != null && isPage) {
            request.getRequestDispatcher(result + ".jsp").forward(request, response);
        } else if (result != null && result.getClass().equals(String.class)) {
            response.setContentType("text/plain");
            response.getWriter().write((String) result);
        } else if (result != null) {
            response.setContentType("application/json");
            response.getWriter().write(objectMapper.writeValueAsString(result));
        }
    }
}
