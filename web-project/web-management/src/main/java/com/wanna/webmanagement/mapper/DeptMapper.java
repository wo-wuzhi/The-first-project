package com.wanna.webmanagement.mapper;

import com.wanna.webmanagement.pojo.Dept;
import org.apache.ibatis.annotations.*;

import java.util.List;

@Mapper
public interface DeptMapper {

    @Select("select id , name , create_time , dept.update_time from dept order by update_time desc")
    List<Dept> findAll();

    @Delete("delete from dept where id = #{id}")
    int deleteById(@Param("id")Integer deptId);

    @Insert("insert into dept(name,create_time,update_time) values (#{name},#{createTime},#{updateTime})")
    void insert(Dept dept);

    @Select("select id,name,create_time,update_time from dept where id = #{id}")
    Dept getById(@Param("id")Integer deptId);

    @Update("update dept set name = #{name}, update_time = #{updateTime} where id = #{id}")
    int update(Dept dept);
}
