package com.wanna.webmanagement.service.impl;

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
import org.springframework.web.bind.annotation.RequestParam;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;

@Service
public class EmpServiceImpl implements EmpService {

    @Autowired
    private EmpMapper empMapper;

    @Autowired
    private EmpExprMapper empExprMapper;

    @Override
    public PageResult<Emp> page(@RequestParam(defaultValue = "1") Integer page,
                                @RequestParam(defaultValue = "10") Integer pageSize,
                                String name, Integer gender, LocalDate begin, LocalDate end) {
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
            exprList.forEach(e->{
                e.setEmpId(emp.getId());
            });
            empExprMapper.insertBatch(exprList);
        }
    }
}
