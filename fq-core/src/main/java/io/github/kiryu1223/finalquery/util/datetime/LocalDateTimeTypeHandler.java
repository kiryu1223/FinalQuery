package io.github.kiryu1223.finalquery.util.datetime;

import io.github.kiryu1223.finalquery.util.ITypeHandler;

import java.sql.*;
import java.time.LocalDateTime;

public class LocalDateTimeTypeHandler implements ITypeHandler<LocalDateTime>
{
    @Override
    public LocalDateTime getValue(ResultSet resultSet, int index) throws SQLException
    {
        return resultSet.getTimestamp(index).toLocalDateTime();
    }

    @Override
    public void setValue(PreparedStatement preparedStatement, int index, LocalDateTime localDateTime) throws SQLException
    {
        preparedStatement.setTimestamp(index, Timestamp.valueOf(localDateTime));
    }

    @Override
    public LocalDateTime getNullableValue(ResultSet resultSet, int index) throws SQLException
    {
        Timestamp timestamp = resultSet.getTimestamp(index);
        if (timestamp == null)
        {
            return null;
        }
        return timestamp.toLocalDateTime();
    }
}
