package io.github.kiryu1223.finalquery.util.datetime;

import io.github.kiryu1223.finalquery.util.ITypeHandler;

import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Timestamp;

public class TimestampTypeHandler implements ITypeHandler<Timestamp>
{
    @Override
    public Timestamp getValue(ResultSet resultSet, int index) throws SQLException
    {
        return resultSet.getTimestamp(index);
    }

    @Override
    public void setValue(PreparedStatement preparedStatement, int index, Timestamp timestamp) throws SQLException
    {
        preparedStatement.setTimestamp(index, timestamp);
    }
}
