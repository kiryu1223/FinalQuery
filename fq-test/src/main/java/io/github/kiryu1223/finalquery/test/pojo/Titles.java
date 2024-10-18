package io.github.kiryu1223.finalquery.test.pojo;

import lombok.Data;

import java.time.LocalDate;

/**
 * 职位
 */
@Data
public class Titles
{
    private int empNumber;
    private String title;
    private LocalDate from;
    private LocalDate to;
}
