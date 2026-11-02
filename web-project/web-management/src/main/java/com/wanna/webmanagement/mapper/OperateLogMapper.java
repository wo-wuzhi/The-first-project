package com.wanna.webmanagement.mapper;

import com.wanna.webmanagement.pojo.OperateLog;
import org.apache.ibatis.annotations.Insert;
import org.apache.ibatis.annotations.Mapper;

@Mapper
public interface OperateLogMapper {

    @Insert("insert into operate_log (operate_emp_id, operate_time, class_name, " +
            "method_name, method_params, return_value, cost_time) " +
            "VALUES (#{operateEmpId},#{operateTime},#{className},#{methodName}," +
            "#{methodParams},#{returnValue},#{costTime})")
    public void insert(OperateLog log);
}
