package com.wanna.webmanagement.service;

import com.wanna.webmanagement.pojo.Emp;
import com.wanna.webmanagement.pojo.PageResult;

public interface EmpService {

    PageResult<Emp> page(Integer page, Integer pageSize);
}
