package org.springbud.aop.annotation;

import java.lang.annotation.*;

@Target(ElementType.TYPE)
@Retention(RetentionPolicy.RUNTIME)
public @interface Aspect {
//    // v1.0 for CGLib
//    Class<? extends Annotation> value() default Annotation.class;

    // v2.0 for AspectJ
    String pointcut() default "";
}
