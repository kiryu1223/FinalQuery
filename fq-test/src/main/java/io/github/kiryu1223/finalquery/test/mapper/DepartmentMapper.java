package io.github.kiryu1223.finalquery.test.mapper;

import io.github.kiryu1223.finalquery.annotation.Mapper;
import io.github.kiryu1223.finalquery.annotation.SqlTemplate;
import io.github.kiryu1223.finalquery.api.BaseMapper;
import io.github.kiryu1223.finalquery.test.pojo.Department;

import java.util.List;

@Mapper
public interface DepartmentMapper extends BaseMapper
{
    @SqlTemplate("SELECT * FROM departments")
    List<Department> getAll();
}
