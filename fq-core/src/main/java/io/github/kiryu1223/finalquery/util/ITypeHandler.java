package io.github.kiryu1223.finalquery.util;

import java.sql.JDBCType;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;

public interface ITypeHandler<T>
{
    T getValue(ResultSet resultSet, int index) throws SQLException;

    void setValue(PreparedStatement preparedStatement, int index, T t) throws SQLException;

    default T getNullableValue(ResultSet resultSet, int index) throws SQLException
    {
        return getValue(resultSet, index);
    }

    default void setNullableValue(PreparedStatement preparedStatement, int index, T t, JDBCType jdbcType) throws SQLException
    {
        if (t == null)
        {
            preparedStatement.setNull(index, jdbcType.getVendorTypeNumber());
        }
        else
        {
            setValue(preparedStatement, index, t);
        }
    }
}
