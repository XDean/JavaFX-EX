package xdean.jfx.ex.bean.annotation;

import static java.lang.annotation.ElementType.*;

import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * This indicated the annotated element should check null or not. It usually use
 * in generic, for example:
 * 
 * <pre>
 * <code>
 * List&#60;&#64;CheckNull String&#62; list = new ArrayList&#60;&#62();
 * </code>
 * </pre>
 * 
 * It means the List's element may be null. But Note that this annotation is
 * just a marker, it is not supported by sonar or findbugs.
 * 
 * @author XDean
 *
 */
@Target({ TYPE_USE, TYPE_PARAMETER, LOCAL_VARIABLE })
@Retention(RetentionPolicy.RUNTIME)
public @interface CheckNull {

}
