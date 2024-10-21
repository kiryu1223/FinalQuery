package io.github.kiryu1223.finalquery.test.controller;

import io.github.kiryu1223.finalquery.annotation.MapperManager;
import io.github.kiryu1223.finalquery.api.Mappers;
import io.github.kiryu1223.finalquery.util.datetime.LocalDateTypeHandler;

import java.time.LocalDate;

@MapperManager
public class MyMapperController extends Mappers
{
    static
    {
        setTypeHandler(LocalDate.class, new LocalDateTypeHandler());
    }
}
