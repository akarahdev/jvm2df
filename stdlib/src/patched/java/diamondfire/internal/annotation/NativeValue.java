package diamondfire.internal.annotation;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * Hints to the reader that this is not an ordinary Java object.
 * Instead, this Java object is a raw DiamondFire value, with special abstractions to make it work.
 */
@Target(ElementType.TYPE)
@Retention(RetentionPolicy.RUNTIME)
public @interface NativeValue {
}
