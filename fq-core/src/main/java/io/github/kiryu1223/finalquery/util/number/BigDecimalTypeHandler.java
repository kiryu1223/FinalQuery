package io.github.kiryu1223.finalquery.util.number;

import io.github.kiryu1223.finalquery.util.ITypeHandler;

import java.math.BigDecimal;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;

public class BigDecimalTypeHandler implements ITypeHandler<BigDecimal>
{
    @Override
    public BigDecimal getValue(ResultSet resultSet, int index) throws SQLException
    {
        return resultSet.getBigDecimal(index);
    }

    @Override
    public void setValue(PreparedStatement preparedStatement, int index, BigDecimal bigDecimal) throws SQLException
    {
        preparedStatement.setBigDecimal(index, bigDecimal);
    }
}
