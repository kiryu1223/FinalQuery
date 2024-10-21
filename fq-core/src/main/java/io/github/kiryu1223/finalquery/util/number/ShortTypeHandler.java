package io.github.kiryu1223.finalquery.util.number;

import io.github.kiryu1223.finalquery.util.ITypeHandler;

import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;

public class ShortTypeHandler implements ITypeHandler<Short>
{
    @Override
    public Short getValue(ResultSet resultSet, int index) throws SQLException
    {
        return resultSet.getShort(index);
    }

    @Override
    public void setValue(PreparedStatement preparedStatement, int index, Short aShort) throws SQLException
    {
        preparedStatement.setShort(index,aShort);
    }

    @Override
    public Short getNullableValue(ResultSet resultSet, int index) throws SQLException
    {
        short aShort = resultSet.getShort(index);
        return resultSet.wasNull() ? null : aShort;
    }
}
