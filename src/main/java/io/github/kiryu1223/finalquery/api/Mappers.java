package io.github.kiryu1223.finalquery.api;

import java.util.HashMap;
import java.util.Map;

public abstract class Mappers
{
    protected static final Map<Class<? extends BaseMapper>, BaseMapper> cache = new HashMap<>();

    protected static <T extends BaseMapper> void setMapper(Class<T> target, T mapper)
    {
        cache.put(target, mapper);
    }

    public <T extends BaseMapper> T getMapper(Class<T> target)
    {
        return (T) cache.get(target);
    }
}
