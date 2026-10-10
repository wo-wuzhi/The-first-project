package com.wanna.webmanagement.service.impl;

import com.wanna.webmanagement.mapper.EmpMapper;
import com.wanna.webmanagement.pojo.JobAnalysis;
import com.wanna.webmanagement.service.ReportService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Map;

@Service
public class ReportServiceImpl implements ReportService {

    @Autowired
    private EmpMapper empMapper;

    @Override
    public JobAnalysis getEmpJobData() {

        List<Map<String,Object>> list = empMapper.countJobData();
        List<String> job = list.stream().map(dataMap -> (String)dataMap.get("pos")).toList();
        List<Integer> count = list.stream().map(dataMap -> (Integer)dataMap.get("num")).toList();
        return new JobAnalysis(job,count);
    }

    @Override
    public List<Map<String, Object>> getEmpGenderData() {
        return empMapper.countGenderData();
    }
}
