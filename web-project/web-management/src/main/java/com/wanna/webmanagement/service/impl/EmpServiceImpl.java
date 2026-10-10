package com.wanna.webmanagement.service.impl;

import com.wanna.webmanagement.exception.BusinessException;
import com.wanna.webmanagement.mapper.EmpExprMapper;
import com.wanna.webmanagement.mapper.EmpMapper;
import com.wanna.webmanagement.pojo.Emp;
import com.wanna.webmanagement.pojo.EmpExpr;
import com.wanna.webmanagement.pojo.PageResult;
import com.wanna.webmanagement.service.EmpService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.CollectionUtils;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

@Service
public class EmpServiceImpl implements EmpService {

    @Autowired
    private EmpMapper empMapper;

    @Autowired
    private EmpExprMapper empExprMapper;

    @Override
    public PageResult<Emp> page(Integer page, Integer pageSize,
                                String name, Integer gender,
                                LocalDate begin, LocalDate end) {
        Long total = empMapper.count(name,gender,begin,end);
        Integer start = (page - 1) * pageSize;
        List<Emp> rows = empMapper.list(start,pageSize,name,gender,begin,end);
        return new PageResult<>(total,rows);
    }

    @Transactional(rollbackFor = Exception.class)
    @Override
    public void save(Emp emp) {

        emp.setCreateTime(LocalDateTime.now());
        emp.setUpdateTime(LocalDateTime.now());
        empMapper.insert(emp);

        List<EmpExpr> exprList=emp.getExprList();
        if(!CollectionUtils.isEmpty(exprList)){
            exprList.forEach(e-> e.setEmpId(emp.getId()));
            empExprMapper.insertBatch(exprList);
        }

    }

    @Transactional(rollbackFor = Exception.class)
    @Override
    public void delete(List<Integer> ids) {

        if(CollectionUtils.isEmpty(ids)){
            throw new BusinessException("请选择要删除的员工");
        }

        List<Integer>existIds=empMapper.getExistIds(ids);
        Set<Integer>existIdsSet=new HashSet<>(existIds);
        List<Integer>errorIds= ids.stream().
                filter(id->!existIdsSet.contains(id)).toList();

        if(!CollectionUtils.isEmpty(errorIds)){
            throw new BusinessException("以下id对应员工不存在"+errorIds);
        }

        empExprMapper.deleteByEmp(existIds);
        empMapper.delete(existIds);
    }

    @Override
    public Emp getById(Integer id) {
        Emp emp=empMapper.getById(id);
        if(emp==null) {
            throw new BusinessException("该员工不存在");
        }
        emp.setExprList(empExprMapper.getById(emp.getId()));
        return emp;
    }

    @Transactional(rollbackFor = Exception.class)
    @Override
    public void update(Emp emp) {

        emp.setUpdateTime(LocalDateTime.now());
        if(empMapper.update(emp)==0){
            throw new BusinessException("要修改的员工不存在");
        }

        List<EmpExpr> exprList=emp.getExprList();
        if(exprList!=null) {
            empExprMapper.deleteExpr(emp.getId());  //exprList非空才删原表

            if(!CollectionUtils.isEmpty(exprList)){  //非空才增加新expr
                exprList.forEach(e-> e.setEmpId(emp.getId()));
                empExprMapper.insertBatch(exprList);
            }
        }
    }
}
