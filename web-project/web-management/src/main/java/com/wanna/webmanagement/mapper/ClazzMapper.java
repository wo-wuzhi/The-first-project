package com.wanna.webmanagement.mapper;

import com.wanna.webmanagement.pojo.Clazz;
import org.apache.ibatis.annotations.*;

import java.time.LocalDate;
import java.util.List;

@Mapper
public interface ClazzMapper {

    Long count(@Param("name") String name,@Param("begin") LocalDate begin,
               @Param("end") LocalDate end);

    List<Clazz> list(@Param("start") Integer start,@Param("pageSize") Integer pageSize,
                     @Param("name") String name,@Param("begin") LocalDate begin,
                     @Param("end") LocalDate end);

    @Delete("delete from clazz where id = #{id}")
    int delete(Integer id);

    @Options(useGeneratedKeys = true,keyProperty = "id")
    @Insert("insert into clazz(name, room, begin_date, end_date, " +
            "master_id, subject, create_time, update_time) " +
            "values (#{name},#{room},#{beginDate},#{endDate},#{masterId}," +
            "#{subject},#{createTime},#{updateTime})")
    void save(Clazz clazz);

    @Select("select id, name, room, begin_date, end_date, master_id," +
            " subject, create_time, update_time from clazz" +
            " where id = #{id}")
    Clazz getById(Integer id);

    int update(Clazz clazz);

    @Select("select id, name, room, begin_date, end_date, master_id, " +
            "subject, create_time, update_time from clazz")
    List<Clazz> findAll();
}
