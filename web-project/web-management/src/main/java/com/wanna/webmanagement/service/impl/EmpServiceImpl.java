package com.wanna.webmanagement.service.impl;

import com.wanna.webmanagement.mapper.EmpMapper;
import com.wanna.webmanagement.pojo.Emp;
import com.wanna.webmanagement.pojo.PageResult;
import com.wanna.webmanagement.service.EmpService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.web.bind.annotation.RequestParam;

import java.util.List;

@Service
public class EmpServiceImpl implements EmpService {

    @Autowired
    private EmpMapper empMapper;

    @Override
    public PageResult<Emp> page(@RequestParam(defaultValue = "1") Integer page,
                                @RequestParam(defaultValue = "10") Integer pageSize){
        Long total = empMapper.count();
        Integer start = (page - 1) * pageSize;
        List<Emp> rows = empMapper.list(start,pageSize);
        return new PageResult<>(total,rows);
    }



}
