package io.github.kiryu1223.finalquery.util.varchar;

import io.github.kiryu1223.finalquery.util.ITypeHandler;

import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;

public class CharTypeHandler implements ITypeHandler<Character>
{
    @Override
    public Character getValue(ResultSet resultSet, int index) throws SQLException
    {
        return resultSet.getString(index).charAt(0);
    }

    @Override
    public void setValue(PreparedStatement preparedStatement, int index, Character character) throws SQLException
    {
        preparedStatement.setString(index, character.toString());
    }

    @Override
    public Character getNullableValue(ResultSet resultSet, int index) throws SQLException
    {
        String string = resultSet.getString(index);
        return string == null ? null : string.charAt(0);
    }
}
