package ca.pjer.spring.boot.jdbi;

import ca.pjer.spring.boot.jdbi.autoconfigure.AutoSqlObjectRegistrar;
import org.springframework.context.annotation.Import;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

@Retention(RetentionPolicy.RUNTIME)
@Target(ElementType.TYPE)
@Import(AutoSqlObjectRegistrar.class)
public @interface EnableAutoSqlObject {
}
