package io.github.kiryu1223.finalquery.test.pojo;

import io.github.kiryu1223.finalquery.annotation.Column;
import lombok.Data;


/**
 * 部门
 */
@Data
public class Department
{
    @Column("dept_no")
    private String number;
    @Column("dept_name")
    private String name;
}
