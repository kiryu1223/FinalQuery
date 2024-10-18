package io.github.kiryu1223.finalquery.api;

import javax.sql.DataSource;
import java.sql.ResultSet;
import java.sql.ResultSetMetaData;
import java.sql.SQLException;
import java.util.*;

public interface BaseMapper
{
    default Map<String, Integer> getIndexMap(ResultSet resultSet, Collection<String> fieldNames) throws SQLException
    {
        Map<String, Integer> indexMap = new HashMap<>();
        ResultSetMetaData metaData = resultSet.getMetaData();
        for (int i = 1; i <= metaData.getColumnCount(); i++)
        {
            String columnLabel = metaData.getColumnLabel(i);
            if(fieldNames.contains(columnLabel)) indexMap.put(columnLabel, i);
        }
        return indexMap;
    }

    default Set<Map.Entry<String, Integer>> getIndexEntrySet(ResultSet resultSet, String... fieldNames) throws SQLException
    {
        return getIndexMap(resultSet, Arrays.asList(fieldNames)).entrySet();
    }

    void setDataSource(DataSource dataSource);
}
