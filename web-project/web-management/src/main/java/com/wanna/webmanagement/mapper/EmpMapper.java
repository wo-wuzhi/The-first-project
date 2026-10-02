package com.wanna.webmanagement.mapper;

import com.wanna.webmanagement.pojo.Emp;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

import java.time.LocalDate;
import java.util.List;

@Mapper
public interface EmpMapper {

    Long count(@Param("name") String name, @Param("gender") Integer gender,
               @Param("begin") LocalDate begin, @Param("end") LocalDate end);

    List<Emp> list(@Param("start") Integer start, @Param("pageSize") Integer pageSize,
                   @Param("name") String name, @Param("gender") Integer gender,
                   @Param("begin") LocalDate begin, @Param("end") LocalDate end);


}
