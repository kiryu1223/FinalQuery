package io.github.kiryu1223.finalquery.test.mapper.impl;

import io.github.kiryu1223.finalquery.test.mapper.DepartmentMapper;
import io.github.kiryu1223.finalquery.test.pojo.Department;

import javax.sql.DataSource;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Map;

public class DepartmentMapperImpl implements DepartmentMapper
{
    private DataSource dataSource;

    @Override
    public void setDataSource(DataSource dataSource)
    {
        this.dataSource = dataSource;
    }

    @Override
    public List<Department> getAll()
    {
        try (Connection connection = dataSource.getConnection())
        {
            try (PreparedStatement preparedStatement = connection.prepareStatement("SELECT * FROM departments"))
            {
                try (ResultSet resultSet = preparedStatement.executeQuery())
                {
                    List<Department> departments = new ArrayList<>();
                    Map<String, Integer> indexMap = getIndexMap(resultSet, Arrays.asList("dept_no", "dept_name"));
                    while (resultSet.next())
                    {
                        Department department = new Department();
                        for (Map.Entry<String, Integer> entry : indexMap.entrySet())
                        {
                            switch (entry.getKey())
                            {
                                case "dept_no":
                                    department.setNumber(resultSet.getString(entry.getValue()));
                                    break;
                                case "dept_name":
                                    department.setName(resultSet.getString(entry.getValue()));
                                    break;
                            }
                        }
                        departments.add(department);
                    }
                    return departments;
                }
            }
            catch (SQLException e)
            {
                connection.rollback();
                throw new RuntimeException(e);
            }
        }
        catch (SQLException e)
        {
            throw new RuntimeException(e);
        }
    }
}
