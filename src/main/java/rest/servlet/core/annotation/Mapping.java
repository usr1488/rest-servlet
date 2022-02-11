package rest.servlet.core.annotation;

import rest.servlet.core.util.HttpMethod;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

@Target(ElementType.METHOD)
@Retention(RetentionPolicy.RUNTIME)
public @interface Mapping {
    String value() default "/";
    HttpMethod method() default HttpMethod.GET;
    String acceptContentType() default "";
    boolean isPage() default false;
}
