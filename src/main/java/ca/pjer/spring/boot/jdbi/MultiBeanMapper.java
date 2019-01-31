package ca.pjer.spring.boot.jdbi;

import org.jdbi.v3.core.mapper.RowMapper;
import org.jdbi.v3.core.mapper.reflect.ReflectionMapperUtil;
import org.jdbi.v3.core.statement.StatementContext;

import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.List;

public class MultiBeanMapper implements RowMapper<MultiBean> {
    private final Class<?>[] types;
    private RowMapper<MultiBean> specializedRowMapper;

    public MultiBeanMapper(Class<?>[] types) {
        this.types = types;
    }

    @Override
    public MultiBean map(ResultSet rs, StatementContext ctx) throws SQLException {
        return specialize(rs, ctx).map(rs, ctx);
    }

    @Override
    public RowMapper<MultiBean> specialize(ResultSet rs, StatementContext ctx) throws SQLException {
        RowMapper<MultiBean> specializedRowMapper = this.specializedRowMapper;

        if (specializedRowMapper == null) {

            RowMapper[] mappers = new RowMapper[types.length];

            List<String> columnNames = ReflectionMapperUtil.getColumnNames(rs);

            int typesIndex = 0;
            int fromIndex = 0;
            for (int i = 0; i < columnNames.size(); i++) {
                String columnName = columnNames.get(i);
                if (columnName.equals("|") || columnName.equals("'|'") || columnName.equals("\"|\"")) {
                    mappers[typesIndex] = new SingleBeanMapper<>(types[typesIndex], fromIndex, i).specialize(rs, ctx);
                    typesIndex++;
                    fromIndex = i + 1;
                }
            }
            mappers[typesIndex] = new SingleBeanMapper<>(types[typesIndex], fromIndex, columnNames.size()).specialize(rs, ctx);

            specializedRowMapper = new SpecializedRowMapper(types, mappers);

            this.specializedRowMapper = specializedRowMapper;
        }

        return specializedRowMapper;
    }

    private static class SpecializedRowMapper implements RowMapper<MultiBean> {
        private final Class<?>[] types;
        private final RowMapper[] mappers;

        SpecializedRowMapper(Class<?>[] types, RowMapper[] mappers) {
            this.types = types;
            this.mappers = mappers;
        }

        @Override
        public MultiBean map(ResultSet rs, StatementContext ctx) throws SQLException {
            MultiBean beans = new MultiBean();
            for (int i = 0; i < types.length; i++) {
                Class<?> type = types[i];
                RowMapper mapper = mappers[i];
                Object bean = mapper.map(rs, ctx);
                beans.put(type, bean);
            }
            return beans;
        }

        @Override
        public RowMapper<MultiBean> specialize(ResultSet rs, StatementContext ctx) {
            return this;
        }
    }
}
