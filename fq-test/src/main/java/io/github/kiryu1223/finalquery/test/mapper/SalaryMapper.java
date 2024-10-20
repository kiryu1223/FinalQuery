package io.github.kiryu1223.finalquery.test.mapper;

import io.github.kiryu1223.finalquery.annotation.Mapper;
import io.github.kiryu1223.finalquery.annotation.SqlTemplate;
import io.github.kiryu1223.finalquery.api.BaseMapper;
import io.github.kiryu1223.finalquery.test.pojo.Employee;
import io.github.kiryu1223.finalquery.test.pojo.Salary;

import java.util.List;

@Mapper
public interface SalaryMapper extends BaseMapper
{
    @SqlTemplate("SELECT `s`.`emp_no`,`s`.`from_date`,`s`.`salary`,`s`.`to_date` FROM `salaries` AS `s`")
    List<Salary> knowIndexGetAll();

    @SqlTemplate("SELECT `s`.* FROM `salaries` AS `s`")
    List<Salary> unKnowIndexGetAll();
}
