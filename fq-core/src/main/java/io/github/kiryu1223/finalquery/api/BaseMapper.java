package io.github.kiryu1223.finalquery.api;

import io.github.kiryu1223.finalquery.service.util.StringIntPair;
import io.github.kiryu1223.finalquery.service.util.StringIntSet;

import javax.sql.DataSource;
import java.sql.ResultSet;
import java.sql.ResultSetMetaData;
import java.sql.SQLException;
import java.util.*;

public interface BaseMapper
{
//    default Map<String, Integer> getIndexMap(ResultSet resultSet, Collection<String> fieldNames) throws SQLException
//    {
//        Map<String, Integer> indexMap = new HashMap<>();
//        ResultSetMetaData metaData = resultSet.getMetaData();
//        for (int i = 1; i <= metaData.getColumnCount(); i++)
//        {
//            String columnLabel = metaData.getColumnLabel(i);
//            if(fieldNames.contains(columnLabel)) indexMap.put(columnLabel, i);
//        }
//        return indexMap;
//    }

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
}
