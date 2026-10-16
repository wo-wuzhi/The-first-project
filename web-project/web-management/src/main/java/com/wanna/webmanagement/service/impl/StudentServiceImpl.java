package com.wanna.webmanagement.service.impl;

import com.wanna.webmanagement.exception.BusinessException;
import com.wanna.webmanagement.mapper.StudentMapper;
import com.wanna.webmanagement.pojo.PageResult;
import com.wanna.webmanagement.pojo.Student;
import com.wanna.webmanagement.service.StudentService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.util.CollectionUtils;

import java.time.LocalDateTime;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

@Service
public class StudentServiceImpl implements StudentService {

    @Autowired
    private StudentMapper studentMapper;

    @Override
    public PageResult<Student> page(Integer page, Integer pageSize,
                                    String name, Integer degree, Integer clazzId) {
        Long total = studentMapper.count(name, degree, clazzId);
        Integer start = (page - 1) * pageSize;
        List<Student> rows = studentMapper.list(start, pageSize, name, degree, clazzId);
        return new PageResult<>(total, rows);
    }

    @Override
    public void save(Student student) {
        student.setCreateTime(LocalDateTime.now());
        student.setUpdateTime(LocalDateTime.now());
        studentMapper.insert(student);
    }

    @Override
    public void delete(List<Integer> ids) {
        if (CollectionUtils.isEmpty(ids)) {
            throw new BusinessException("请选择要删除的学员");
        }
        // 先查出实际存在的 id，把不存在的报给前端
        List<Integer> existIds = studentMapper.getExistIds(ids);
        Set<Integer> existIdSet = new HashSet<>(existIds);
        List<Integer> notExistIds = ids.stream()
                .filter(id -> !existIdSet.contains(id))
                .toList();
        if (!CollectionUtils.isEmpty(notExistIds)) {
            throw new BusinessException("以下id对应学员不存在" + notExistIds);
        }
        studentMapper.deleteByIds(existIds);
    }

    @Override
    public Student getById(Integer id) {
        Student student = studentMapper.getById(id);
        if (student == null) {
            throw new BusinessException("查询的学员不存在 id为 " + id);
        }
        return student;
    }

    @Override
    public void update(Student student) {
        student.setUpdateTime(LocalDateTime.now());
        if (studentMapper.update(student) == 0) {
            throw new BusinessException("要修改的学员不存在");
        }
    }

    @Override
    public void addViolation(Integer id, Integer score) {
        if(score==null||score<=0){
            throw new BusinessException("扣分应为正数");
        }
        if(studentMapper.addViolation(id,score)==0){
            throw new BusinessException("处分学员不存在");
        }
    }
}
