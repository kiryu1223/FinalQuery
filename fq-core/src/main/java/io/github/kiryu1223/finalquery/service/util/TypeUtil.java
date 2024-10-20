package io.github.kiryu1223.finalquery.service.util;

import com.sun.tools.javac.code.Type;

import javax.lang.model.type.TypeKind;
import java.math.BigDecimal;
import java.math.BigInteger;
import java.sql.Timestamp;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;

public class TypeUtil
{
    public static boolean isByte(Type type)
    {
        return type.getKind() == TypeKind.BYTE || type.toString().equals(Byte.class.getCanonicalName());
    }

    public static boolean isShort(Type type)
    {
        return type.getKind() == TypeKind.SHORT || type.toString().equals(Short.class.getCanonicalName());
    }

    public static boolean isInt(Type type)
    {
        return type.getKind() == TypeKind.INT || type.toString().equals(Integer.class.getCanonicalName());
    }

    public static boolean isLong(Type type)
    {
        return type.getKind() == TypeKind.LONG || type.toString().equals(Long.class.getCanonicalName());
    }

    public static boolean isBool(Type type)
    {
        return type.getKind() == TypeKind.BOOLEAN || type.toString().equals(Boolean.class.getCanonicalName());
    }

    public static boolean isChar(Type type)
    {
        return type.getKind() == TypeKind.CHAR || type.toString().equals(Character.class.getCanonicalName());
    }

    public static boolean isFloat(Type type)
    {
        return type.getKind() == TypeKind.FLOAT || type.toString().equals(Float.class.getCanonicalName());
    }

    public static boolean isDouble(Type type)
    {
        return type.getKind() == TypeKind.DOUBLE || type.toString().equals(Double.class.getCanonicalName());
    }

    public static boolean isString(Type type)
    {
        return type.toString().equals(String.class.getCanonicalName());
    }

    public static boolean isDate(Type type)
    {
        return type.toString().equals(java.sql.Date.class.getCanonicalName());
    }

    public static boolean isLocalDate(Type type)
    {
        return type.toString().equals(LocalDate.class.getCanonicalName());
    }

    public static boolean isTime(Type type)
    {
        return type.toString().equals(java.sql.Time.class.getCanonicalName());
    }

    public static boolean isLocalTime(Type type)
    {
        return type.toString().equals(LocalTime.class.getCanonicalName());
    }

    public static boolean isTimestamp(Type type)
    {
        return type.toString().equals(Timestamp.class.getCanonicalName());
    }

    public static boolean isLocalDateTime(Type type)
    {
        return type.toString().equals(LocalDateTime.class.getCanonicalName());
    }

    public static boolean isBigDecimal(Type type)
    {
        return type.toString().equals(BigDecimal.class.getCanonicalName());
    }

    public static boolean isBigInteger(Type type)
    {
        return type.toString().equals(BigInteger.class.getCanonicalName());
    }

    public static boolean isEnum(Type type)
    {
        return type.asElement().isEnum();
    }
}
