package com.wanna.webmanagement.service;

import com.wanna.webmanagement.pojo.PageResult;
import com.wanna.webmanagement.pojo.Student;

import java.util.List;

public interface StudentService {

    PageResult<Student> page(Integer page, Integer pageSize,
                             String name, Integer degree, Integer clazzId);

    void save(Student student);

    void delete(List<Integer> ids);

    Student getById(Integer id);

    void update(Student student);

    void addViolation(Integer id, Integer score);
}
