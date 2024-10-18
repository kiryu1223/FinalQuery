package io.github.kiryu1223.finalquery.test.mapper;

import io.github.kiryu1223.finalquery.api.Mappers;
import io.github.kiryu1223.finalquery.test.mapper.impl.DepartmentMapperImpl;

// 用户写的
public class MyMapperController extends Mappers
{
    // 编译生成的
    static {
        Mappers.setMapper(DepartmentMapper.class,new DepartmentMapperImpl());
    }
}
