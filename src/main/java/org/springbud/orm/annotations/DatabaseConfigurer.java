package org.springbud.orm.annotations;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;


@Target(ElementType.TYPE)
@Retention(RetentionPolicy.RUNTIME)
public @interface DatabaseConfigurer {
    String host() default "127.0.0.1";
    int port() default 3306;
    String database() default "defaultDB";
    String username() default "root";
    String password();
}
