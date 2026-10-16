package com.wanna.webmanagement.controller;

import com.wanna.webmanagement.pojo.Clazz;
import com.wanna.webmanagement.pojo.PageResult;
import com.wanna.webmanagement.pojo.Result;
import com.wanna.webmanagement.service.ClazzService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDate;
import java.util.List;

@Slf4j
@RestController
@RequestMapping("/clazzs")
public class ClazzController {

    @Autowired
    private ClazzService clazzService;

    @GetMapping
    public Result pageShow(@RequestParam(defaultValue = "1") Integer page,
                           @RequestParam(defaultValue = "10") Integer pageSize,
                           String name,
                           @DateTimeFormat(pattern = "yyyy-MM-dd") LocalDate begin,
                           @DateTimeFormat(pattern = "yyyy-MM-dd") LocalDate end){
        log.info("分页 {} {} 查询班级 条件：{} {} {}",page,pageSize,name,begin,end);
        PageResult<Clazz>pageResult=clazzService.pageShow(page,pageSize,name,begin,end);
        return Result.success(pageResult);
    }

    @DeleteMapping("/{id}")
    public Result delete(@PathVariable Integer id){
        log.info("删除id为 {} 的班级",id);
        clazzService.delete(id);
        return Result.success();
    }

    @PostMapping
    public Result save(@RequestBody Clazz clazz){
        log.info("添加班级 {}",clazz);
        clazzService.save(clazz);
        return Result.success();
    }

    @GetMapping("/{id}")
    public Result findById(@PathVariable Integer id){
        log.info("查询id为 {} 的班级",id);
        Clazz clazz = clazzService.getById(id);
        return Result.success(clazz);
    }

    @PutMapping
    public Result update(@RequestBody Clazz clazz){
        log.info("修改班级 {}",clazz);
        clazzService.update(clazz);
        return Result.success();
    }

    @GetMapping("/list")
    public Result findAll(){
        log.info("查询所有班级");
        List<Clazz>list = clazzService.findAll();
        return Result.success(list);
    }
}
