package io.github.kiryu1223.finalquery.util.datetime;

import io.github.kiryu1223.finalquery.util.ITypeHandler;

import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Time;

public class TimeTypeHandler implements ITypeHandler<Time>
{
    @Override
    public Time getValue(ResultSet resultSet, int index) throws SQLException
    {
        return resultSet.getTime(index);
    }

    @Override
    public void setValue(PreparedStatement preparedStatement, int index, Time time) throws SQLException
    {
        preparedStatement.setTime(index, time);
    }
}
