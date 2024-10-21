package io.github.kiryu1223.finalquery.util;

import java.sql.JDBCType;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;

public class UnKnowTypeHandler<T> implements ITypeHandler<T>
{
    @Override
    public T getValue(ResultSet resultSet, int index) throws SQLException
    {
        return (T) resultSet.getObject(index);
    }

    @Override
    public void setValue(PreparedStatement preparedStatement, int index, T t) throws SQLException
    {
        preparedStatement.setObject(index, t);
    }
}
