package ca.pjer.spring.boot.jdbi;

import org.jdbi.v3.core.config.ConfigRegistry;
import org.jdbi.v3.core.mapper.RowMapper;
import org.jdbi.v3.core.mapper.RowMappers;
import org.jdbi.v3.sqlobject.config.Configurer;

import java.lang.annotation.Annotation;
import java.lang.reflect.Method;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.stream.Collectors;
import java.util.stream.Stream;

public class RegisterMultiBeanMapperImpl implements Configurer {
    private final Map<String, RowMapper<MultiBean>> cache = new ConcurrentHashMap<>();

    @Override
    public void configureForType(ConfigRegistry registry, Annotation annotation, Class<?> sqlObjectType) {
        configure(registry, annotation, sqlObjectType.toString());
    }

    @Override
    public void configureForMethod(ConfigRegistry registry, Annotation annotation, Class<?> sqlObjectType, Method method) {
        configure(registry, annotation, sqlObjectType.toString() + "|" + method.toString());
    }

    private void configure(ConfigRegistry registry, Annotation annotation, String key) {
        Class<?>[] types = ((RegisterMultiBeanMapper) annotation).value();
        key += "|" + Stream.of(types).map(Object::toString).collect(Collectors.joining("-"));
        RowMapper<MultiBean> mapper = cache.computeIfAbsent(key, k -> new MultiBeanMapper(types));
        registry.get(RowMappers.class).register(mapper);
    }
}
