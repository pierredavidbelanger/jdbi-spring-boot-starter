package ca.pjer.spring.boot.jdbi;

import org.jdbi.v3.core.mapper.ColumnMapper;
import org.jdbi.v3.core.mapper.RowMapper;
import org.jdbi.v3.core.mapper.reflect.ColumnNameMatcher;
import org.jdbi.v3.core.mapper.reflect.ReflectionMapperUtil;
import org.jdbi.v3.core.mapper.reflect.ReflectionMappers;
import org.jdbi.v3.core.statement.StatementContext;
import org.springframework.beans.BeanUtils;

import java.beans.PropertyDescriptor;
import java.lang.reflect.Method;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;

public class SingleBeanMapper<T> implements RowMapper<T> {
    private final Class<T> type;
    private final int fromIndex;
    private final int toIndex;

    private RowMapper<T> specializedRowMapper;

    public SingleBeanMapper(Class<T> type) {
        this(type, -1, -1);
    }

    SingleBeanMapper(Class<T> type, int fromIndex, int toIndex) {
        this.type = type;
        this.fromIndex = fromIndex;
        this.toIndex = toIndex;
    }

    @Override
    public T map(ResultSet rs, StatementContext ctx) throws SQLException {
        return specialize(rs, ctx).map(rs, ctx);
    }

    @Override
    public RowMapper<T> specialize(ResultSet rs, StatementContext ctx) throws SQLException {
        RowMapper<T> specializedRowMapper = this.specializedRowMapper;

        if (specializedRowMapper == null) {

            List<String> columnNames = ReflectionMapperUtil.getColumnNames(rs);
            List<ColumnNameMatcher> columnNameMatchers = ctx.getConfig(ReflectionMappers.class).getColumnNameMatchers();
            PropertyDescriptor[] propertyDescriptors = BeanUtils.getPropertyDescriptors(type);

            List<Mapping> mappings = new ArrayList<>(propertyDescriptors.length);

            int fromIndex = this.fromIndex;
            int toIndex = this.toIndex;
            if (fromIndex < 0) {
                fromIndex = 0;
                toIndex = columnNames.size();
            }

            for (int i = fromIndex; i < toIndex; i++) {
                String columnName = columnNames.get(i);
                ColumnMapper columnMapper = null;
                for (PropertyDescriptor propertyDescriptor : propertyDescriptors) {
                    String javaName = propertyDescriptor.getName();
                    for (ColumnNameMatcher columnNameMatcher : columnNameMatchers) {
                        if (columnNameMatcher.columnNameMatches(columnName, javaName)) {
                            columnMapper = ctx.findColumnMapperFor(propertyDescriptor.getWriteMethod().getGenericParameterTypes()[0]).orElse(this::mapRawObject);
                            break;
                        }
                    }
                    if (columnMapper != null) {
                        mappings.add(new Mapping(i + 1, columnMapper, propertyDescriptor.getWriteMethod()));
                        break;
                    }
                }
            }

            specializedRowMapper = new SpecializedRowMapper<>(type, mappings);

            this.specializedRowMapper = specializedRowMapper;
        }

        return specializedRowMapper;
    }

    private Object mapRawObject(ResultSet r, int columnNumber, StatementContext ctx) {
        try {
            return r.getObject(columnNumber);
        } catch (SQLException e) {
            throw new RuntimeException(e);
        }
    }

    private static class SpecializedRowMapper<T> implements RowMapper<T> {
        private final Class<T> type;
        private final List<Mapping> mappings;

        SpecializedRowMapper(Class<T> type, List<Mapping> mappings) {
            this.type = type;
            this.mappings = mappings;
        }

        @Override
        public T map(ResultSet rs, StatementContext ctx) throws SQLException {
            T bean;
            try {
                bean = type.newInstance();
            } catch (ReflectiveOperationException e) {
                throw new IllegalArgumentException(e);
            }
            for (Mapping mapping : mappings) {
                Object value = mapping.columnMapper.map(rs, mapping.columnNumber, ctx);
                try {
                    mapping.beanWriteMethod.invoke(bean, value);
                } catch (ReflectiveOperationException e) {
                    throw new IllegalArgumentException(e);
                }
            }
            return bean;
        }

        @Override
        public RowMapper<T> specialize(ResultSet rs, StatementContext ctx) {
            return this;
        }
    }

    private static class Mapping {
        final int columnNumber;
        final ColumnMapper columnMapper;
        final Method beanWriteMethod;

        Mapping(int columnNumber, ColumnMapper columnMapper, Method beanWriteMethod) {
            this.columnNumber = columnNumber;
            this.columnMapper = columnMapper;
            this.beanWriteMethod = beanWriteMethod;
        }
    }
}
