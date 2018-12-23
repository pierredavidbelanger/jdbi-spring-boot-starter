package ca.pjer.spring.boot.jdbi;

import org.jdbi.v3.sqlobject.config.ConfiguringAnnotation;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

@ConfiguringAnnotation(RegisterMultiBeanMapperImpl.class)
@Retention(RetentionPolicy.RUNTIME)
@Target({ElementType.TYPE, ElementType.METHOD})
public @interface RegisterMultiBeanMapper {
    Class<?>[] value();
}
