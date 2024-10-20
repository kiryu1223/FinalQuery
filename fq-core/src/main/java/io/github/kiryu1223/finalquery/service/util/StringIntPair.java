package io.github.kiryu1223.finalquery.service.util;

public class StringIntPair
{
    private final String name;
    private final int index;

    public StringIntPair(String name, int index)
    {
        this.name = name;
        this.index = index;
    }

    public String getName()
    {
        return name;
    }

    public int getIndex()
    {
        return index;
    }
}
