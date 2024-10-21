package io.github.kiryu1223.finalquery.util.datetime;


import io.github.kiryu1223.finalquery.util.ITypeHandler;

import java.sql.*;
import java.time.LocalDate;

public class LocalDateTypeHandler implements ITypeHandler<LocalDate>
{
    @Override
    public LocalDate getValue(ResultSet resultSet, int index) throws SQLException
    {
        return resultSet.getDate(index).toLocalDate();
    }

    @Override
    public void setValue(PreparedStatement preparedStatement, int index, LocalDate localDate) throws SQLException
    {
        preparedStatement.setDate(index, Date.valueOf(localDate));
    }

    @Override
    public LocalDate getNullableValue(ResultSet resultSet, int index) throws SQLException
    {
        Date date = resultSet.getDate(index);
        if (date == null)
        {
            return null;
        }
        return date.toLocalDate();
    }
}
