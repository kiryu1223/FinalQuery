package io.github.kiryu1223.finalquery.util.datetime;

import io.github.kiryu1223.finalquery.util.ITypeHandler;

import java.sql.*;
import java.time.LocalTime;

public class LocalTimeTypeHandler implements ITypeHandler<LocalTime>
{
    @Override
    public LocalTime getValue(ResultSet resultSet, int index) throws SQLException
    {
        return resultSet.getTime(index).toLocalTime();
    }

    @Override
    public void setValue(PreparedStatement preparedStatement, int index, LocalTime localTime) throws SQLException
    {
        preparedStatement.setTime(index, Time.valueOf(localTime));
    }

    @Override
    public LocalTime getNullableValue(ResultSet resultSet, int index) throws SQLException
    {
        Time time = resultSet.getTime(index);
        if (time == null)
        {
            return null;
        }
        return time.toLocalTime();
    }
}
