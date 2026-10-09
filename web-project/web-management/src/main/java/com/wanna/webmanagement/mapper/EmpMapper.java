package com.wanna.webmanagement.mapper;

import com.wanna.webmanagement.pojo.Emp;
import com.wanna.webmanagement.pojo.EmpExpr;
import org.apache.ibatis.annotations.*;

import java.time.LocalDate;
import java.util.List;

@Mapper
public interface EmpMapper {

    Long count(@Param("name") String name, @Param("gender") Integer gender,
               @Param("begin") LocalDate begin, @Param("end") LocalDate end);

    List<Emp> list(@Param("start") Integer start, @Param("pageSize") Integer pageSize,
                   @Param("name") String name, @Param("gender") Integer gender,
                   @Param("begin") LocalDate begin, @Param("end") LocalDate end);

    @Options(useGeneratedKeys = true, keyProperty = "id")    //自增主键回填给插入项
    @Insert("insert into emp(username, name, gender, phone, job, salary," +
            " image, entry_date, dept_id, create_time, update_time) " +
            "values (#{username},#{name},#{gender},#{phone},#{job},#{salary}," +
            "#{image},#{entryDate},#{deptId},#{createTime},#{updateTime})")
    void insert(Emp emp);

    void delete(List<Integer> ids);

    List<Integer> getExistIds(@Param("ids")List<Integer> ids);

    @Select("select id, username, name, gender, phone, job, salary, image, entry_date, dept_id," +
            " create_time, update_time from emp where id = #{id}")
    Emp getById(Integer id);

    @Update("update emp set username = #{username}, name = #{name}, gender = #{gender}, " +
            "phone = #{phone}, job = #{job}, salary = #{salary}, image = #{image}, " +
            "entry_date = #{entryDate}, dept_id = #{deptId}, update_time = #{updateTime} " +
            "where id = #{id}")
    int update(Emp emp);


}
