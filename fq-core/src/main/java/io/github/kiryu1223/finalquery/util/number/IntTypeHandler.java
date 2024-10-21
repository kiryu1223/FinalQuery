package io.github.kiryu1223.finalquery.util.number;

import io.github.kiryu1223.finalquery.util.ITypeHandler;

import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;

public class IntTypeHandler implements ITypeHandler<Integer>
{
    @Override
    public Integer getValue(ResultSet resultSet, int index) throws SQLException
    {
        return resultSet.getInt(index);
    }

    @Override
    public void setValue(PreparedStatement preparedStatement, int index, Integer integer) throws SQLException
    {
        preparedStatement.setInt(index, integer);
    }

    @Override
    public Integer getNullableValue(ResultSet resultSet, int index) throws SQLException
    {
        int value = resultSet.getInt(index);
        return resultSet.wasNull() ? null : value;
    }
}
