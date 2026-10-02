package com.wanna.webmanagement.service;

import com.wanna.webmanagement.pojo.Emp;
import com.wanna.webmanagement.pojo.PageResult;

import java.time.LocalDate;


public interface EmpService {

    PageResult<Emp> page(Integer page, Integer pageSize, String name, Integer gender, LocalDate begin, LocalDate end );
}
