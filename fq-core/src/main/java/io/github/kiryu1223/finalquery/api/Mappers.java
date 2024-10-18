package io.github.kiryu1223.finalquery.api;

import javax.sql.DataSource;
import java.util.HashMap;
import java.util.Map;

public abstract class Mappers
{
    private static final Map<Class<? extends BaseMapper>, BaseMapper> cache = new HashMap<>();

    protected static <T extends BaseMapper> void setMapper(Class<T> target, T mapper)
    {
        cache.put(target, mapper);
    }

    private DataSource dataSource;

    public void setDataSource(DataSource dataSource)
    {
        assert dataSource != null : "dataSource should be not null";
        this.dataSource = dataSource;
    }

    public <T extends BaseMapper> T getMapper(Class<T> target)
    {
        T mapper = (T) cache.get(target);
        mapper.setDataSource(dataSource);
        return mapper;
    }
}
