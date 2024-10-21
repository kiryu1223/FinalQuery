package io.github.kiryu1223.finalquery.util.number;

import io.github.kiryu1223.finalquery.util.ITypeHandler;

import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;

public class DoubleTypeHandler implements ITypeHandler<Double>
{
    @Override
    public Double getValue(ResultSet resultSet, int index) throws SQLException
    {
        return resultSet.getDouble(index);
    }

    @Override
    public void setValue(PreparedStatement preparedStatement, int index, Double aDouble) throws SQLException
    {
        preparedStatement.setDouble(index,aDouble);
    }

    @Override
    public Double getNullableValue(ResultSet resultSet, int index) throws SQLException
    {
        double aDouble = resultSet.getDouble(index);
        return resultSet.wasNull() ? null : aDouble;
    }
}
