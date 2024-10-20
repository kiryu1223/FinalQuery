package io.github.kiryu1223.finalquery.test.pojo;

import io.github.kiryu1223.finalquery.annotation.Column;
import lombok.Data;

import java.time.LocalDate;
import java.util.List;

/**
 * 员工
 */
@Data
public class Employee
{
    @Column(value = "emp_no")
    private int number;
    @Column("birth_date")
    private LocalDate birthDay;
    @Column("first_name")
    private String firstName;
    @Column("last_name")
    private String lastName;
    private Gender gender;
    @Column("hire_date")
    private LocalDate hireDay;
}
