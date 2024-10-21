package io.github.kiryu1223.finalquery.util.datetime;

import io.github.kiryu1223.finalquery.util.ITypeHandler;

import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Timestamp;
import java.util.Date;

public class UtilDateHandler implements ITypeHandler<Date>
{
    @Override
    public Date getValue(ResultSet resultSet, int index) throws SQLException
    {
        Timestamp timestamp = resultSet.getTimestamp(index);
        return Date.from(timestamp.toInstant());
    }

    @Override
    public void setValue(PreparedStatement preparedStatement, int index, Date date) throws SQLException
    {
        preparedStatement.setTimestamp(index, Timestamp.from(date.toInstant()));
    }

    @Override
    public Date getNullableValue(ResultSet resultSet, int index) throws SQLException
    {
        Timestamp timestamp = resultSet.getTimestamp(index);
        if (timestamp == null)
        {
            return null;
        }
        return Date.from(timestamp.toInstant());
    }
}
