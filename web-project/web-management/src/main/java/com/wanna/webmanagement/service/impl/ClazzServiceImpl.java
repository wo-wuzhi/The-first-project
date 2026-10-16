package com.wanna.webmanagement.service.impl;

import com.wanna.webmanagement.exception.BusinessException;
import com.wanna.webmanagement.mapper.ClazzMapper;
import com.wanna.webmanagement.pojo.Clazz;
import com.wanna.webmanagement.pojo.PageResult;
import com.wanna.webmanagement.service.ClazzService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;

@Service
public class ClazzServiceImpl implements ClazzService {

    @Autowired
    private ClazzMapper clazzMapper;

    @Override
    public PageResult<Clazz> pageShow(Integer page, Integer pageSize,
                                      String name, LocalDate begin, LocalDate end) {
        Long total = clazzMapper.count(name,begin,end);
        Integer start = (page-1)*pageSize;
        List<Clazz>rows = clazzMapper.list(start,pageSize,name,begin,end);
        return new PageResult<Clazz>(total,rows);
    }

    @Override
    public void delete(Integer id) {
        int row = clazzMapper.delete(id);
        if(row==0){
            throw new BusinessException("要删除的班级不存在");
        }
    }

    @Override
    public void save(Clazz clazz) {
        clazz.setCreateTime(LocalDateTime.now());
        clazz.setUpdateTime(LocalDateTime.now());
        clazzMapper.save(clazz);
    }

    @Override
    public Clazz getById(Integer id) {
        Clazz clazz = clazzMapper.getById(id);
        if(clazz==null){
            throw new BusinessException("查询的班级不存在 id为 "+id);
        }
        return  clazz;
    }

    @Override
    public void update(Clazz clazz) {
        clazz.setUpdateTime(LocalDateTime.now());
        if(clazzMapper.update(clazz)==0){
            throw new BusinessException("修改的班级不存在");
        }
    }

    @Override
    public List<Clazz> findAll() {
        return clazzMapper.findAll();
    }
}
