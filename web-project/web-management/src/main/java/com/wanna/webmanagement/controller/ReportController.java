package com.wanna.webmanagement.controller;

import com.wanna.webmanagement.pojo.JobAnalysis;
import com.wanna.webmanagement.pojo.Result;
import com.wanna.webmanagement.service.ReportService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;
import java.util.Map;

@Slf4j
@RestController
@RequestMapping("/report")
public class ReportController {

    @Autowired
    private ReportService reportService;

    @GetMapping("/empJobData")
    public Result getEmpJobData(){
        log.info("统计员工职位人数");
        JobAnalysis jobAnalysis = reportService.getEmpJobData();
        return Result.success(jobAnalysis);
    }

    @GetMapping("/empGenderData")
    public Result getEmpGenderData(){
        log.info("统计员工性别对应人数");
        List<Map<String,Object>> genderAnalysis = reportService.getEmpGenderData();
        return Result.success(genderAnalysis);
    }
}
