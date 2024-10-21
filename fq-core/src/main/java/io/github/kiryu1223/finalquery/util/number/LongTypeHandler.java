package io.github.kiryu1223.finalquery.util.number;

import io.github.kiryu1223.finalquery.util.ITypeHandler;

import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;

public class LongTypeHandler implements ITypeHandler<Long>
{
    @Override
    public Long getValue(ResultSet resultSet, int index) throws SQLException
    {
        return resultSet.getLong(index);
    }

    @Override
    public void setValue(PreparedStatement preparedStatement, int index, Long aLong) throws SQLException
    {
        preparedStatement.setLong(index,aLong);
    }

    @Override
    public Long getNullableValue(ResultSet resultSet, int index) throws SQLException
    {
        long aLong = resultSet.getLong(index);
        return resultSet.wasNull() ? null : aLong;
    }
}
