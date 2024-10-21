package io.github.kiryu1223.finalquery.util.number;

import io.github.kiryu1223.finalquery.util.ITypeHandler;

import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;

public class BoolTypeHandler implements ITypeHandler<Boolean>
{
    @Override
    public Boolean getValue(ResultSet resultSet, int index) throws SQLException
    {
        return resultSet.getBoolean(index);
    }

    @Override
    public void setValue(PreparedStatement preparedStatement, int index, Boolean aBoolean) throws SQLException
    {
        preparedStatement.setBoolean(index, aBoolean);
    }

    @Override
    public Boolean getNullableValue(ResultSet resultSet, int index) throws SQLException
    {
        boolean aBoolean = resultSet.getBoolean(index);
        return resultSet.wasNull() ? null : aBoolean;
    }
}
