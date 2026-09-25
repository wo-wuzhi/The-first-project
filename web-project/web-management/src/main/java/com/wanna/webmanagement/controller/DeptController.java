package com.wanna.webmanagement.controller;

import com.wanna.webmanagement.pojo.Dept;
import com.wanna.webmanagement.pojo.Result;
import com.wanna.webmanagement.service.DeptService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@Controller
@ResponseBody
public class DeptController {

    @Autowired
    private DeptService deptService;

    @GetMapping( "/depts")
    public Result select(){
        System.out.println("查询全部部门数据");
        List<Dept> deptList= deptService.findAll();
        return Result.success(deptList);
    }

    @DeleteMapping("/depts")
    public Result delete(@RequestParam("id") Integer deptId){
        System.out.println("删除部门"+deptId);
        deptService.deleteById(deptId);
        return Result.success();
    }

    @PostMapping("/depts")
    public Result insert(@RequestBody Dept dept){
        System.out.println("添加部门"+dept);
        deptService.insert(dept);
        return Result.success();
    }

    @GetMapping("/depts/{id}")
    public Result getById(@PathVariable("id") Integer deptId){
        System.out.println("查询到id为"+deptId+"的部门");
        Dept dept=deptService.getById(deptId);
        return Result.success(dept);
    }

    @PutMapping("/depts")
    public Result update(@RequestBody Dept dept){
        System.out.println("修改"+dept.getName()+"id为"+dept.getId());
        deptService.update(dept);
        return  Result.success();
    }
}
