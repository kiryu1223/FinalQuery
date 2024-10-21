package io.github.kiryu1223.finalquery.util.varchar;

import io.github.kiryu1223.finalquery.util.ITypeHandler;

import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;

public class StringTypeHandler implements ITypeHandler<String>
{
    @Override
    public String getValue(ResultSet resultSet, int index) throws SQLException
    {
        return resultSet.getString(index);
    }

    @Override
    public void setValue(PreparedStatement preparedStatement, int index, String s) throws SQLException
    {
        preparedStatement.setString(index,s);
    }
}
