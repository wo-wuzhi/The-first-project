package com.wanna.webmanagement.controller;

import com.wanna.webmanagement.pojo.PageResult;
import com.wanna.webmanagement.pojo.Result;
import com.wanna.webmanagement.pojo.Student;
import com.wanna.webmanagement.service.StudentService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@Slf4j
@RestController
@RequestMapping("/students")
public class StudentController {

    @Autowired
    private StudentService studentService;

    @GetMapping
    public Result page(@RequestParam(defaultValue = "1") Integer page,
                       @RequestParam(defaultValue = "10") Integer pageSize,
                       String name,
                       Integer degree,
                       Integer clazzId) {
        log.info("分页查询学员 {}/{} 条件：name={}, degree={}, clazzId={}",
                page, pageSize, name, degree, clazzId);
        PageResult<Student> pageResult = studentService.page(page, pageSize, name, degree, clazzId);
        return Result.success(pageResult);
    }

    @PostMapping
    public Result save(@RequestBody Student student) {
        log.info("新增学员 {}", student);
        studentService.save(student);
        return Result.success();
    }

    @GetMapping("/{id}")
    public Result findById(@PathVariable Integer id) {
        log.info("查询id为 {} 的学员", id);
        Student student = studentService.getById(id);
        return Result.success(student);
    }

    @PutMapping
    public Result update(@RequestBody Student student) {
        log.info("修改学员 {}", student);
        studentService.update(student);
        return Result.success();
    }

    @DeleteMapping("/{ids}")
    public Result delete(@PathVariable List<Integer> ids) {
        log.info("删除学员 ids为 {}", ids);
        studentService.delete(ids);
        return Result.success();
    }

    @PutMapping("/violation/{id}/{score}")
    public Result violation(@PathVariable Integer id, @PathVariable Integer score) {
        log.info("违纪处理 id {} score {}", id, score);
        studentService.violation(id,score);
        return Result.success();
    }
}
