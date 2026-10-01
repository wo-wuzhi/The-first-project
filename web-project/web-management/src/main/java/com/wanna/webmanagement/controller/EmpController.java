package com.wanna.webmanagement.controller;

import com.wanna.webmanagement.mapper.EmpMapper;
import com.wanna.webmanagement.pojo.Emp;
import com.wanna.webmanagement.pojo.PageResult;
import com.wanna.webmanagement.pojo.Result;
import com.wanna.webmanagement.service.EmpService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@Slf4j
@RestController
@RequestMapping("/emps")
public class EmpController {

    @Autowired
    private EmpService empService;

    @GetMapping
    public Result page(Integer page, Integer pageSize) {
        log.info("分页查询 {} {}",page,pageSize);
        PageResult<Emp> pageResult = empService.page(page,pageSize);
        return Result.success(pageResult);
    }

}
