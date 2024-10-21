package io.github.kiryu1223.finalquery.util.number;

import io.github.kiryu1223.finalquery.util.ITypeHandler;

import java.math.BigDecimal;
import java.math.BigInteger;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;

public class BigIntegerTypeHandler implements ITypeHandler<BigInteger>
{
    @Override
    public BigInteger getValue(ResultSet resultSet, int index) throws SQLException
    {
        BigDecimal value = resultSet.getBigDecimal(index);
        return value.toBigInteger();
    }

    @Override
    public void setValue(PreparedStatement preparedStatement, int index, BigInteger bigInteger) throws SQLException
    {
        preparedStatement.setBigDecimal(index, new BigDecimal(bigInteger));
    }

    @Override
    public BigInteger getNullableValue(ResultSet resultSet, int index) throws SQLException
    {
        BigDecimal value = resultSet.getBigDecimal(index);
        return resultSet.wasNull() ? null : value.toBigInteger();
    }
}
