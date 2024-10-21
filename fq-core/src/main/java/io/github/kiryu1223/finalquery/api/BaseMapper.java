package io.github.kiryu1223.finalquery.api;

import io.github.kiryu1223.finalquery.service.util.StringIntPair;
import io.github.kiryu1223.finalquery.service.util.StringIntSet;
import io.github.kiryu1223.finalquery.util.ITypeHandler;

import javax.sql.DataSource;
import java.sql.ResultSet;
import java.sql.ResultSetMetaData;
import java.sql.SQLException;
import java.util.Arrays;
import java.util.List;

public interface BaseMapper
{
    default StringIntSet getIndexEntrySet(ResultSet resultSet, String... fieldNames) throws SQLException
    {
        ResultSetMetaData metaData = resultSet.getMetaData();
        StringIntSet stringSet = new StringIntSet();
        List<String> list = Arrays.asList(fieldNames);
        for (int i = 1; i <= metaData.getColumnCount(); i++)
        {
            String columnLabel = metaData.getColumnLabel(i);
            if (list.contains(columnLabel))
            {
                stringSet.add(new StringIntPair(columnLabel, i));
            }
        }
        return stringSet;
    }

    void setDataSource(DataSource dataSource);

    default <T> ITypeHandler<T> getTypeHandler(Class<T> type)
    {
        return Mappers.getTypeHandler(type);
    }
}
