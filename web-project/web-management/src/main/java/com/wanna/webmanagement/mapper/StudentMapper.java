package com.wanna.webmanagement.mapper;

import com.wanna.webmanagement.pojo.Student;
import org.apache.ibatis.annotations.*;

import java.util.List;
import java.util.Map;

@Mapper
public interface StudentMapper {

    Long count(@Param("name") String name,
               @Param("degree") Integer degree,
               @Param("clazzId") Integer clazzId);

    List<Student> list(@Param("start") Integer start,
                       @Param("pageSize") Integer pageSize,
                       @Param("name") String name,
                       @Param("degree") Integer degree,
                       @Param("clazzId") Integer clazzId);

    @Options(useGeneratedKeys = true, keyProperty = "id")
    @Insert("insert into student(name, no, gender, phone, id_card, is_college, " +
            "address, degree, graduation_date, clazz_id, create_time, update_time) " +
            "values (#{name},#{no},#{gender},#{phone},#{idCard},#{isCollege}," +
            "#{address},#{degree},#{graduationDate},#{clazzId},#{createTime},#{updateTime})")
    void insert(Student student);

    Student getById(Integer id);

    int update(Student student);

    List<Integer> getExistIds(@Param("ids") List<Integer> ids);

    int deleteByIds(@Param("ids") List<Integer> ids);

    @Update("update student set violation_count = violation_count+1," +
            "violation_score = violation_score + #{score}," +
            "update_time = now() " +
            "where id = #{id}")
    int addViolation(@Param("id") Integer id,@Param("score") Integer score);

    List<Map<String, Object>> countDegreeData();

    List<Map<String, Object>> countClazzData();
}
