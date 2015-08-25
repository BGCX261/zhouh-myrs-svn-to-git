/**
 * 
 */
package org.myfw.spvi.annotation;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;



/**主键
 * @author zhouhui
 */
@Retention(RetentionPolicy.RUNTIME)
@Target(ElementType.FIELD)
public @interface Id{
	public enum IdPolicy{AUTO,IDENTITY }
	IdPolicy value() default IdPolicy.AUTO;

}
