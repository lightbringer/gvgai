package net.gvgai.vgdl;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

@Retention( RetentionPolicy.RUNTIME )
@Target( ElementType.TYPE )
public @interface SpriteInfo {
    /**
     * @return a string that hints at how a runtime should visualize objects
     * of this class. How this string is interpreted depends on the VGDL Runtime. Some
     * implementations mights even ignore this field.
     */
    String resourceInfo() default "";
}
