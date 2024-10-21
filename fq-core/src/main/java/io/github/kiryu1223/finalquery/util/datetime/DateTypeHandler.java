package io.github.kiryu1223.finalquery.util.datetime;

import io.github.kiryu1223.finalquery.util.ITypeHandler;

import java.sql.Date;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;

public class DateTypeHandler implements ITypeHandler<Date>
{
    @Override
    public Date getValue(ResultSet resultSet, int index) throws SQLException
    {
        return resultSet.getDate(index);
    }

    @Override
    public void setValue(PreparedStatement preparedStatement, int index, Date date) throws SQLException
    {
        preparedStatement.setDate(index,date);
    }
}
