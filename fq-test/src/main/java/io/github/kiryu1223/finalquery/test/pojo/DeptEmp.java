package io.github.kiryu1223.finalquery.test.pojo;

import lombok.Data;

import java.time.LocalDate;

/**
 * 部门与员工多对多的中间表
 */
@Data
public class DeptEmp
{
    private int empNumber;
    private String deptNumber;
    private LocalDate from;
    private LocalDate to;
}
