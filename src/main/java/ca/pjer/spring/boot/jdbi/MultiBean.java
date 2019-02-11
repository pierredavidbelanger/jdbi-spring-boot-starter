package ca.pjer.spring.boot.jdbi;

import java.io.Serializable;
import java.util.HashMap;
import java.util.Map;
import java.util.Set;

public class MultiBean implements Cloneable, Serializable {
    private final Map<Class, Object> beans = new HashMap<>();

    void put(Class type, Object bean) {
        beans.put(type, bean);
    }

    public Set<Class> typeSet() {
        return beans.keySet();
    }

    public <T> T get(Class<T> type) {
        return type.cast(beans.get(type));
    }

    @Override
    public int hashCode() {
        return beans.hashCode();
    }

    @Override
    public boolean equals(Object o) {
        return beans.equals(o);
    }

    @Override
    public Object clone() throws CloneNotSupportedException {
        return super.clone();
    }

    @Override
    public String toString() {
        return beans.toString();
    }
}
