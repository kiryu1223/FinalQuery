package io.github.kiryu1223.finalquery.service.util;

import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class SqlUtil
{
    public static String clearColumn(String name)
    {
        if (name.contains("."))
        {
            // `a`.`abc` -> `abc`
            name=name.split("\\.")[1];
        }
        name = name.replace("`", "");
        name = name.replace("'", "");
        name = name.replace("\"", "");
        return name;
    }
}
