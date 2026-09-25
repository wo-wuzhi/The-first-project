package com.wanna.webmanagement.service;

import com.wanna.webmanagement.pojo.Dept;

import java.util.List;

public interface DeptService {

    List<Dept> findAll();

    void deleteById(Integer deptId);

    void insert(Dept dept);

    Dept getById(Integer deptId);

    void update(Dept dept);
}
