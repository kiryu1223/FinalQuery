package io.github.kiryu1223.finalquery.service.util;

import com.sun.tools.javac.code.Type;

import static io.github.kiryu1223.finalquery.service.util.TypeUtil.*;

public class FieldMetaData
{
    private final String fieldName;
    private final String columnName;
    private final String setterName;
    private final String resultSetGetterName;
    private final Type type;

    public FieldMetaData(String fieldName, String columnName, Type type)
    {
        this.fieldName = fieldName;
        this.columnName = columnName;
        this.setterName = "set" + Character.toUpperCase(fieldName.charAt(0)) + fieldName.substring(1);
        this.type = type;
        this.resultSetGetterName = resultSetName(type);
    }

    public String getFieldName()
    {
        return fieldName;
    }

    public String getColumnName()
    {
        return columnName;
    }

    public String getSetterName()
    {
        return setterName;
    }

    public Type getType()
    {
        return type;
    }

    public String getResultSetGetterName()
    {
        return resultSetGetterName;
    }

    private String resultSetName(Type type)
    {
        if (type instanceof Type.ClassType)
        {
            if (isString(type))
            {
                return "getString";
            }
            else if (isDate(type) || isLocalDate(type))
            {
                return "getDate";
            }
            else if (isTime(type) || isLocalTime(type))
            {
                return "getTime";
            }
            else if (isTimestamp(type) || isLocalDateTime(type))
            {
                return "getTimestamp";
            }
        }
        else if (type instanceof Type.JCPrimitiveType)
        {
            Type.JCPrimitiveType jcPrimitiveType = (Type.JCPrimitiveType) type;
            switch (jcPrimitiveType.getKind())
            {
                case BYTE:
                    return "getByte";
                case CHAR:
                    return "getString";
                case SHORT:
                    return "getShort";
                case INT:
                    return "getInt";
                case LONG:
                    return "getLong";
                case FLOAT:
                    return "getFloat";
                case DOUBLE:
                    return "getDouble";
                case BOOLEAN:
                    return "getBoolean";
            }
        }
        return "getObject";
    }
}
