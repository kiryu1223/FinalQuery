package io.github.kiryu1223.finalquery.service.util;

import com.sun.tools.javac.code.Symbol;
import com.sun.tools.javac.code.Type;
import io.github.kiryu1223.finalquery.annotation.Column;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

public class TypeMetaData
{
    private static final Map<Type, TypeMetaData> cache = new ConcurrentHashMap<>();

    public static TypeMetaData get(Type type)
    {
        TypeMetaData typeMetaData = cache.get(type);
        if (typeMetaData == null)
        {
            typeMetaData = new TypeMetaData(type);
            cache.put(type, typeMetaData);
        }
        return typeMetaData;
    }

    private final Type type;
    private final List<FieldMetaData> fieldMetaData = new ArrayList<>();

    private TypeMetaData(Type type)
    {
        this.type = type;
        Symbol.ClassSymbol classSymbol = (Symbol.ClassSymbol) type.asElement();
        for (Symbol enclosedElement : classSymbol.getEnclosedElements())
        {
            if (enclosedElement instanceof Symbol.VarSymbol)
            {
                Symbol.VarSymbol var = (Symbol.VarSymbol) enclosedElement;
                Column column = var.getAnnotation(Column.class);
                String fieldName = var.getSimpleName().toString();
                String columnName = column == null ? fieldName : column.value();
                FieldMetaData data = new FieldMetaData(fieldName, columnName, var.asType());
                fieldMetaData.add(data);
            }
        }
    }

    public Type getType()
    {
        return type;
    }

    public FieldMetaData getFieldMetaDataByColumnName(String name)
    {
        return fieldMetaData.stream()
                .filter(f -> f.getColumnName().equals(name))
                .findFirst()
                .orElseThrow(RuntimeException::new);
    }

    public List<FieldMetaData> getFieldMetaData()
    {
        return fieldMetaData;
    }
}
