package com.wanna.webmanagement.service.impl;

import com.wanna.webmanagement.exception.BusinessException;
import com.wanna.webmanagement.mapper.DeptMapper;
import com.wanna.webmanagement.pojo.Dept;
import com.wanna.webmanagement.service.DeptService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.List;

@Service
public class DeptServiceImpl implements DeptService {

    @Autowired
    private DeptMapper deptMapper;

    @Override
    public List<Dept> findAll() {
        return deptMapper.findAll();
    }

    @Override
    public void deleteById(Integer deptId){
        int rows=deptMapper.deleteById(deptId);
        if(rows==0){
            throw new BusinessException("要删除的部门不存在，id = " + deptId);
        }
    }

    @Override
    public void insert(Dept dept){
        dept.setCreateTime(LocalDateTime.now());
        dept.setUpdateTime(LocalDateTime.now());
        deptMapper.insert(dept);
    }

    @Override
    public Dept getById(Integer deptId){
        Dept dept=deptMapper.getById(deptId);
        if(dept==null){
            throw new BusinessException("部门不存在，id = " + deptId);
        }
        return dept;
    }

    @Override
    public void update(Dept dept){
        dept.setUpdateTime(LocalDateTime.now());
        if(deptMapper.update(dept)==0){
            throw new BusinessException("要修改的部门不存在，id = " + dept.getId());
        }
    }
}
