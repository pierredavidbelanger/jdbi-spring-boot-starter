# jdbi-spring-boot-starter
This is a Spring Boot Starter for auto creating Jdbi and SqlObject

## Usage

Add this to your Spring Boot project's `pom.xml`:

```xml
<dependency>
    <groupId>ca.pjer</groupId>
    <artifactId>jdbi-spring-boot-starter</artifactId>
    <version>1.2.0</version>
</dependency>
```

Then after [configuring a data source](https://docs.spring.io/spring-boot/docs/current/reference/html/boot-features-sql.html), you will be able to inject `Jdbi` like this:

```java
import org.jdbi.v3.core.Jdbi;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

@Component
class MyService {
    @Autowired Jdbi jdbi;
}
```

Now if you want to auto create and inject your [SQL Objects](http://jdbi.org/#_sql_objects) into other components:

Annotate your main class with `@EnableAutoSqlObject`:

```java
import ca.pjer.spring.boot.jdbi.EnableAutoSqlObject;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

@SpringBootApplication
@EnableAutoSqlObject
public class App {
    public static void main(String[] args) {
        SpringApplication.run(App.class, args);
    }
}
```

Also, annotate your `SQL Objects` (those you want auto created and injected) with `@AutoSqlObject`:

```java
import ca.pjer.spring.boot.jdbi.AutoSqlObject;
import org.jdbi.v3.sqlobject.statement.SqlQuery;

@AutoSqlObject
public interface MyDAO {
    @SqlQuery("SELECT * FROM my_table")
    List<MyTable> findAll();
}
```

Then you will now be able to inject them like this:

```java
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

@Component
class MyService {
    @Autowired MyDAO myDAO;
}
```

