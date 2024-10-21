package io.github.kiryu1223.finalquery.util.number;

import io.github.kiryu1223.finalquery.util.ITypeHandler;

import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;

public class ByteTypeHandler implements ITypeHandler<Byte>
{
    @Override
    public Byte getValue(ResultSet resultSet, int index) throws SQLException
    {
        return resultSet.getByte(index);
    }

    @Override
    public void setValue(PreparedStatement preparedStatement, int index, Byte aByte) throws SQLException
    {
        preparedStatement.setByte(index, aByte);
    }

    @Override
    public Byte getNullableValue(ResultSet resultSet, int index) throws SQLException
    {
        byte aByte = resultSet.getByte(index);
        return resultSet.wasNull() ? null : aByte;
    }
}
