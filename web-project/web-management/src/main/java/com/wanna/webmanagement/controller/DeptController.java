package com.wanna.webmanagement.controller;

import com.wanna.webmanagement.pojo.Dept;
import com.wanna.webmanagement.pojo.Result;
import com.wanna.webmanagement.service.DeptService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@Slf4j
@Controller
@ResponseBody
public class DeptController {

    @Autowired
    private DeptService deptService;

    @GetMapping( "/depts")
    public Result select(){
        log.info("查询全部部门数据");
        List<Dept> deptList= deptService.findAll();
        return Result.success(deptList);
    }

    @DeleteMapping("/depts")
    public Result delete(@RequestParam("id") Integer deptId){
        log.info("删除部门  {}",deptId);
        deptService.deleteById(deptId);
        return Result.success();
    }

    @PostMapping("/depts")
    public Result insert(@RequestBody Dept dept){
        log.info("添加部门 {}",dept);
        deptService.insert(dept);
        return Result.success();
    }

    @GetMapping("/depts/{id}")
    public Result getById(@PathVariable("id") Integer deptId){  //PathVariable:将{id}赋予deptId
        log.info("查询到id为 {} 的部门",deptId);
        Dept dept=deptService.getById(deptId);
        return Result.success(dept);
    }

    @PutMapping("/depts")
    public Result update(@RequestBody Dept dept){
        log.info("修改 {} id为 {}",dept.getName(),dept.getId());
        deptService.update(dept);
        return  Result.success();
    }
}
