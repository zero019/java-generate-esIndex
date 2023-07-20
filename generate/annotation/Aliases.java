package generate.annotation;

import org.springframework.data.annotation.Persistent;

import java.lang.annotation.*;

import static java.lang.annotation.ElementType.*;

/**
 * @author zeronly 2023/7/19
 */
@Retention(RetentionPolicy.RUNTIME)
@Target(ElementType.TYPE)
public @interface Aliases {
    String name() default "";
}
