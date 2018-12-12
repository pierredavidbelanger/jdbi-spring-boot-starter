package ca.pjer.spring.boot.jdbi;

import org.jdbi.v3.core.Jdbi;
import org.jdbi.v3.sqlobject.SqlObjectPlugin;
import org.springframework.boot.autoconfigure.condition.ConditionalOnMissingBean;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.jdbc.datasource.TransactionAwareDataSourceProxy;

import javax.sql.DataSource;

@Configuration
public class JdbiConfiguration {

    @Bean
    @ConditionalOnMissingBean(Jdbi.class)
    @SuppressWarnings("SpringJavaInjectionPointsAutowiringInspection")
    Jdbi jdbi(DataSource dataSource) {
        Jdbi jdbi = Jdbi.create(new TransactionAwareDataSourceProxy(dataSource));
        jdbi.installPlugin(new SqlObjectPlugin());
        return jdbi;
    }
}
