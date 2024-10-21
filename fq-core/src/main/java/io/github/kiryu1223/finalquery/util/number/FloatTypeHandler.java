package io.github.kiryu1223.finalquery.util.number;

import io.github.kiryu1223.finalquery.util.ITypeHandler;

import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;

public class FloatTypeHandler implements ITypeHandler<Float>
{
    @Override
    public Float getValue(ResultSet resultSet, int index) throws SQLException
    {
        return resultSet.getFloat(index);
    }

    @Override
    public void setValue(PreparedStatement preparedStatement, int index, Float aFloat) throws SQLException
    {
        preparedStatement.setFloat(index, aFloat);
    }

    @Override
    public Float getNullableValue(ResultSet resultSet, int index) throws SQLException
    {
        float aFloat = resultSet.getFloat(index);
        return resultSet.wasNull() ? null : aFloat;
    }
}
