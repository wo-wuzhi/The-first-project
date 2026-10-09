package com.wanna.webmanagement.mapper;

import com.wanna.webmanagement.pojo.EmpExpr;
import org.apache.ibatis.annotations.Delete;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Select;

import java.util.List;

@Mapper
public interface EmpExprMapper {

    void insertBatch(List<EmpExpr> exprList);

    void deleteByEmp(List<Integer> ids);

    @Select("select id, emp_id, begin, end, company, job from emp_expr where emp_id = #{id}")
    List<EmpExpr> getById(Integer id);

    @Delete("delete from emp_expr where emp_id = #{id}")
    void deleteExpr(Integer id);
}
