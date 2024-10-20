package io.github.kiryu1223.finalquery.test;

import com.zaxxer.hikari.HikariDataSource;
import io.github.kiryu1223.finalquery.service.util.SqlUtil;
import io.github.kiryu1223.finalquery.service.util.StringIntPair;
import io.github.kiryu1223.finalquery.service.util.StringIntSet;
import io.github.kiryu1223.finalquery.test.mapper.DepartmentMapper;
import io.github.kiryu1223.finalquery.test.controller.MyMapperController;
import io.github.kiryu1223.finalquery.test.mapper.EmployeeMapper;
import io.github.kiryu1223.finalquery.test.mapper.SalaryMapper;
import io.github.kiryu1223.finalquery.test.pojo.Department;
import io.github.kiryu1223.finalquery.test.pojo.Employee;
import io.github.kiryu1223.finalquery.test.pojo.Gender;
import io.github.kiryu1223.finalquery.test.pojo.Salary;
import net.sf.jsqlparser.JSQLParserException;
import net.sf.jsqlparser.expression.Alias;
import net.sf.jsqlparser.parser.CCJSqlParserUtil;
import net.sf.jsqlparser.statement.Statement;
import net.sf.jsqlparser.statement.select.PlainSelect;
import net.sf.jsqlparser.statement.select.Select;
import net.sf.jsqlparser.statement.select.SelectItem;

import javax.sql.DataSource;
import java.sql.*;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

public class Main
{
    public static void main(String[] args) throws JSQLParserException
    {
        //pgsql
//        HikariDataSource dataSource = new HikariDataSource();
//        dataSource.setDriverClassName("org.postgresql.Driver");
//        dataSource.setJdbcUrl("jdbc:postgresql://localhost:5432/");
//        dataSource.setUsername("postgres");
//        dataSource.setPassword("root");

        HikariDataSource dataSource = new HikariDataSource();
        dataSource.setJdbcUrl("jdbc:mysql://127.0.0.1:3306/employees?rewriteBatchedStatements=true&useUnicode=true&characterEncoding=UTF-8");
        dataSource.setUsername("root");
        dataSource.setPassword("root");
        dataSource.setDriverClassName("com.mysql.cj.jdbc.Driver");

//        MyMapperController myMapperController = new MyMapperController();
//        myMapperController.setDataSource(dataSource);
//        SalaryMapper mapper = myMapperController.getMapper(SalaryMapper.class);
//        {
//            long start = System.currentTimeMillis();
//            List<Salary> all = mapper.knowIndexGetAll();
//            System.out.println("knowIndexGetAll耗时" + (System.currentTimeMillis() - start) + "毫秒");
//            System.out.println("返回" + all.size() + "条数据");
//        }
//        {
//            long start = System.currentTimeMillis();
//            List<Salary> all = mapper.unKnowIndexGetAll();
//            System.out.println("unKnowIndexGetAll耗时" + (System.currentTimeMillis() - start) + "毫秒");
//            System.out.println("返回" + all.size() + "条数据");
//        }

        {
            long start = System.currentTimeMillis();
            List<Salary> salaries = test1(dataSource);
            System.out.println("test1耗时" + (System.currentTimeMillis() - start) + "毫秒");
            System.out.println("返回" + salaries.size() + "条数据");
        }
        {
            long start = System.currentTimeMillis();
            List<Salary> salaries = test2(dataSource);
            System.out.println("test2耗时" + (System.currentTimeMillis() - start) + "毫秒");
            System.out.println("返回" + salaries.size() + "条数据");
        }
    }

    private static List<Salary> test1(DataSource dataSource)
    {
        try (Connection connection = dataSource.getConnection())
        {
            PreparedStatement preparedStatement = connection.prepareStatement("SELECT `s`.`emp_no`,`s`.`from_date`,`s`.`salary`,`s`.`to_date` FROM `salaries` AS `s`");
            ResultSet resultSet = preparedStatement.executeQuery();
            long start = System.currentTimeMillis();
            List<Salary> result = new ArrayList<Salary>();
            while (resultSet.next())
            {
                Salary t = new Salary();

                int value1 = resultSet.getInt(1);
                t.setEmpNumber(value1);

                Date temp2 = resultSet.getDate(2);
                LocalDate value2 = temp2.toLocalDate();
                t.setFrom(value2);

                int value3 = resultSet.getInt(3);
                t.setSalary(value3);

                Date temp4 = resultSet.getDate(4);
                LocalDate value4 = temp4.toLocalDate();
                t.setTo(value4);

                result.add(t);
            }
            System.out.println(System.currentTimeMillis() - start);
            return result;
        }
        catch (SQLException e)
        {
            throw new RuntimeException(e);
        }
    }

    private static List<Salary> test2(DataSource dataSource)
    {
        try (Connection connection = dataSource.getConnection())
        {
            PreparedStatement preparedStatement = connection.prepareStatement("SELECT `s`.`emp_no`,`s`.`from_date`,`s`.`salary`,`s`.`to_date` FROM `salaries` AS `s`");
            ResultSet resultSet = preparedStatement.executeQuery();
            StringIntSet entrySet = getIndexEntrySet(resultSet, "emp_no", "from_date", "salary", "to_date");
            long start = System.currentTimeMillis();
            List<Salary> result = new ArrayList<Salary>();
            while (resultSet.next())
            {
                Salary t = new Salary();

                for (StringIntPair stringIntPair : entrySet)
                {
                    switch (stringIntPair.getName())
                    {
                        case "emp_no":
                            int value1 = resultSet.getInt(stringIntPair.getIndex());
                            t.setEmpNumber(value1);
                            break;
                        case "from_date":
                            Date temp2 = resultSet.getDate(stringIntPair.getIndex());
                            LocalDate value2 = temp2.toLocalDate();
                            t.setFrom(value2);
                            break;
                        case "salary":
                            int value3 = resultSet.getInt(stringIntPair.getIndex());
                            t.setSalary(value3);
                            break;
                        case "to_date":
                            Date temp4 = resultSet.getDate(stringIntPair.getIndex());
                            LocalDate value4 = temp4.toLocalDate();
                            t.setTo(value4);
                            break;
                    }
                }
                result.add(t);
            }
            System.out.println(System.currentTimeMillis() - start);
            return result;
        }
        catch (SQLException e)
        {
            throw new RuntimeException(e);
        }
    }

    private static StringIntSet getIndexEntrySet(ResultSet resultSet, String... fieldNames) throws SQLException
    {
        ResultSetMetaData metaData = resultSet.getMetaData();
        StringIntSet stringSet = new StringIntSet();
        List<String> list = Arrays.asList(fieldNames);
        for (int i = 1; i <= metaData.getColumnCount(); i++)
        {
            String columnLabel = metaData.getColumnLabel(i);
            if (list.contains(columnLabel))
            {
                stringSet.add(new StringIntPair(columnLabel, i));
            }
        }
        return stringSet;
    }
}
