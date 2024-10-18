package io.github.kiryu1223.finalquery.test;

import com.zaxxer.hikari.HikariDataSource;
import io.github.kiryu1223.finalquery.test.mapper.DepartmentMapper;
import io.github.kiryu1223.finalquery.test.mapper.MyMapperController;
import io.github.kiryu1223.finalquery.test.pojo.Department;

import java.util.List;

public class Main
{
    public static void main(String[] args)
    {
        HikariDataSource dataSource = new HikariDataSource();
        dataSource.setDriverClassName("org.postgresql.Driver");
        dataSource.setJdbcUrl("jdbc:postgresql://localhost:5432/");
        dataSource.setUsername("postgres");
        dataSource.setPassword("root");

        MyMapperController myMapperController = new MyMapperController();
        DepartmentMapper mapper = myMapperController.getMapper(DepartmentMapper.class);

        List<Department> all = mapper.getAll();
        for (Department department : all)
        {
            System.out.println(department);
        }
    }
}
