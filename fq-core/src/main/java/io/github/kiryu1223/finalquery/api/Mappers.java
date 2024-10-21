package io.github.kiryu1223.finalquery.api;

import io.github.kiryu1223.finalquery.util.ITypeHandler;
import io.github.kiryu1223.finalquery.util.UnKnowTypeHandler;
import io.github.kiryu1223.finalquery.util.datetime.*;
import io.github.kiryu1223.finalquery.util.number.*;
import io.github.kiryu1223.finalquery.util.varchar.CharTypeHandler;
import io.github.kiryu1223.finalquery.util.varchar.StringTypeHandler;

import javax.sql.DataSource;
import java.math.BigDecimal;
import java.math.BigInteger;
import java.sql.Date;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.util.HashMap;
import java.util.Map;

public abstract class Mappers
{
    private static final Map<Class<? extends BaseMapper>, BaseMapper> mapperCache = new HashMap<>();

    protected static <T extends BaseMapper> void setMapper(Class<T> target, T mapper)
    {
        mapperCache.put(target, mapper);
    }

    private static final Map<Class<?>, ITypeHandler<?>> typeHandlerCache = new HashMap<>();
    private static final UnKnowTypeHandler<?> unKnowTypeHandler = new UnKnowTypeHandler<>();

    protected static <T> void setTypeHandler(Class<T> target, ITypeHandler<T> typeHandler)
    {
        typeHandlerCache.put(target, typeHandler);
    }

    static
    {
        // str
        setTypeHandler(char.class, new CharTypeHandler());
        setTypeHandler(Character.class, new CharTypeHandler());
        setTypeHandler(String.class, new StringTypeHandler());

        // number
        setTypeHandler(byte.class, new ByteTypeHandler());
        setTypeHandler(Byte.class, new ByteTypeHandler());
        setTypeHandler(short.class, new ShortTypeHandler());
        setTypeHandler(Short.class, new ShortTypeHandler());
        setTypeHandler(int.class, new IntTypeHandler());
        setTypeHandler(Integer.class, new IntTypeHandler());
        setTypeHandler(long.class, new LongTypeHandler());
        setTypeHandler(Long.class, new LongTypeHandler());
        setTypeHandler(float.class, new FloatTypeHandler());
        setTypeHandler(Float.class, new FloatTypeHandler());
        setTypeHandler(double.class, new DoubleTypeHandler());
        setTypeHandler(Double.class, new DoubleTypeHandler());
        setTypeHandler(BigDecimal.class, new BigDecimalTypeHandler());
        setTypeHandler(BigInteger.class, new BigIntegerTypeHandler());
        setTypeHandler(boolean.class, new BoolTypeHandler());
        setTypeHandler(Boolean.class, new BoolTypeHandler());

        // Date&Time
        setTypeHandler(java.util.Date.class, new UtilDateHandler());
        setTypeHandler(Date.class, new DateTypeHandler());
        setTypeHandler(LocalDate.class, new LocalDateTypeHandler());
        setTypeHandler(LocalTime.class, new LocalTimeTypeHandler());
        setTypeHandler(LocalDateTime.class, new LocalDateTimeTypeHandler());
    }

    private final ThreadLocal<DataSource> localDataSource = new ThreadLocal<>();

    public void setDataSource(DataSource dataSource)
    {
        assert dataSource != null : "dataSource should be not null";
        this.localDataSource.set(dataSource);
    }

    public <T extends BaseMapper> T getMapper(Class<T> target)
    {
        T mapper = (T) mapperCache.get(target);
        mapper.setDataSource(localDataSource.get());
        return mapper;
    }

    public static <T> ITypeHandler<T> getTypeHandler(Class<T> target)
    {
        ITypeHandler<T> typeHandler = (ITypeHandler<T>) typeHandlerCache.get(target);
        if (typeHandler == null)
        {
            return (ITypeHandler<T>) unKnowTypeHandler;
        }
        return typeHandler;
    }
}
