package xdean.jfx.ex.bean.annotation;

import static java.lang.annotation.ElementType.PARAMETER;

import java.lang.annotation.Documented;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * Indicate the parameter is not referenced by the result
 * 
 * @author XDean
 *
 */
@Documented
@Target(PARAMETER)
@Retention(RetentionPolicy.RUNTIME)
public @interface NotRef {

}
