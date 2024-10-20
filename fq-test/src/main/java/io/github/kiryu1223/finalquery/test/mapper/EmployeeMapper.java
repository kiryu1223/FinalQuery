package io.github.kiryu1223.finalquery.test.mapper;

import io.github.kiryu1223.finalquery.annotation.Mapper;
import io.github.kiryu1223.finalquery.annotation.SqlTemplate;
import io.github.kiryu1223.finalquery.api.BaseMapper;
import io.github.kiryu1223.finalquery.test.pojo.Department;
import io.github.kiryu1223.finalquery.test.pojo.Employee;

import java.util.List;

@Mapper
public interface EmployeeMapper extends BaseMapper
{
    @SqlTemplate("SELECT * FROM employees")
    List<Employee> getAll();
}
