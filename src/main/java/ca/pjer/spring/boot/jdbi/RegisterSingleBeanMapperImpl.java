package ca.pjer.spring.boot.jdbi;

import org.jdbi.v3.core.config.ConfigRegistry;
import org.jdbi.v3.core.mapper.RowMapper;
import org.jdbi.v3.core.mapper.RowMapperFactory;
import org.jdbi.v3.core.mapper.RowMappers;
import org.jdbi.v3.sqlobject.config.Configurer;

import java.lang.annotation.Annotation;
import java.lang.reflect.Method;
import java.util.Map;
import java.util.Optional;
import java.util.concurrent.ConcurrentHashMap;

public class RegisterSingleBeanMapperImpl implements Configurer {

    private final Map<String, RowMapperFactory> cache = new ConcurrentHashMap<>();

    @Override
    public void configureForType(ConfigRegistry registry, Annotation annotation, Class<?> sqlObjectType) {
        configure(registry, annotation, sqlObjectType.toString());
    }

    @Override
    public void configureForMethod(ConfigRegistry registry, Annotation annotation, Class<?> sqlObjectType, Method method) {
        configure(registry, annotation, sqlObjectType.toString() + "|" + method.toString());
    }

    private void configure(ConfigRegistry registry, Annotation annotation, String key) {
        Class<?> type = ((RegisterSingleBeanMapper) annotation).value();
        key += "|" + type.toString();
        RowMapperFactory factory = cache.computeIfAbsent(key, k -> createRowMapperFactory(type, new SingleBeanMapper<>(type)));
        registry.get(RowMappers.class).register(factory);
    }

    private RowMapperFactory createRowMapperFactory(Class<?> type, RowMapper<?> rowMapper) {
        Optional<RowMapper<?>> notEmpty = Optional.of(rowMapper);
        Optional<RowMapper<?>> empty = Optional.empty();
        return (actualType, config) -> actualType.equals(type) ? notEmpty : empty;
    }
}
