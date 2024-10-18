package io.github.kiryu1223.finalquery.test.pojo;

import lombok.Data;

import java.time.LocalDate;

/**
 * 薪水
 */
@Data
public class Salary
{
    private int empNumber;
    private int salary;
    private LocalDate from;
    private LocalDate to;
    private Employee employee;
}
