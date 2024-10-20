package io.github.kiryu1223.finalquery.test.pojo;

import io.github.kiryu1223.finalquery.annotation.Column;
import lombok.Data;

import java.time.LocalDate;

/**
 * 薪水
 */
@Data
public class Salary
{
    @Column("emp_no")
    private int empNumber;
    private int salary;
    @Column("from_date")
    private LocalDate from;
    @Column("to_date")
    private LocalDate to;
}
