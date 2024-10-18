package io.github.kiryu1223.finalquery.test.pojo;

import lombok.Data;

import java.time.LocalDate;
import java.util.List;

/**
 * 员工
 */
@Data
public class Employee
{
    private int number;
    private LocalDate birthDay;
    private String firstName;
    private String lastName;
    private Gender gender;
    private LocalDate hireDay;
    private List<Salary> salaries;
    private List<DeptEmp> deptEmp;
}
