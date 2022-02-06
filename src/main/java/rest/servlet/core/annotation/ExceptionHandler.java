package rest.servlet.core.annotation;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

@Target({ElementType.METHOD, ElementType.TYPE})
@Retention(RetentionPolicy.RUNTIME)
@Component
public @interface ExceptionHandler {
    // ignored on class level declaration
    Class<? extends Throwable> value() default Throwable.class;
}
