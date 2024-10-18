package io.github.kiryu1223.finalquery.test.pojo;

import lombok.Data;

import java.time.LocalDate;

/**
 * 部门管理者
 */
@Data
public class DeptManager
{
    private int managerNumber;
    private String deptNumber;
    private LocalDate from;
    private LocalDate to;
}
