package ca.pjer.spring.boot.jdbi;

import org.jdbi.v3.core.Jdbi;
import org.springframework.beans.factory.FactoryBean;
import org.springframework.beans.factory.InitializingBean;

public class SqlObjectFactoryBean implements FactoryBean, InitializingBean {

    private Jdbi jdbi;
    private Class type;

    private Object object;

    public Jdbi getJdbi() {
        return jdbi;
    }

    public void setJdbi(Jdbi jdbi) {
        this.jdbi = jdbi;
    }

    public Class getType() {
        return type;
    }

    public void setType(Class type) {
        this.type = type;
    }

    @Override
    public void afterPropertiesSet() {
        //noinspection unchecked
        object = getJdbi().onDemand(getType());
    }

    @Override
    public Object getObject() {
        return object;
    }

    @Override
    public Class<?> getObjectType() {
        return getType();
    }

    @Override
    public boolean isSingleton() {
        return true;
    }
}
