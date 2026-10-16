package com.wanna.webmanagement.service.impl;

import com.wanna.webmanagement.mapper.EmpMapper;
import com.wanna.webmanagement.mapper.StudentMapper;
import com.wanna.webmanagement.pojo.ClazzAnalysis;
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

    @Autowired
    private StudentMapper studentMapper;

    @Override
    public JobAnalysis getEmpJobData() {

        List<Map<String,Object>> list = empMapper.countJobData();
        List<String> job = list.stream()
                .map(dataMap -> (String)dataMap.get("pos")).toList();
        List<Integer> data = list.stream()
                .map(dataMap -> ((Number)dataMap.get("num")).intValue()).toList();
        return new JobAnalysis(job,data);
    }

    @Override
    public List<Map<String, Object>> getEmpGenderData() {
        return empMapper.countGenderData();
    }

    @Override
    public List<Map<String, Object>> getStudentDegreeData() {
        return studentMapper.countDegreeData();
    }

    @Override
    public ClazzAnalysis getStudentCountData() {
        List<Map<String, Object>> list = studentMapper.countClazzData();
        List<String> clazzList = list.stream()
                .map(dataMap -> (String) dataMap.get("clazzName"))
                .toList();
        // count() 返回的是 Long，用 Number 兜住再转，避免 ClassCastException
        List<Integer> dataList = list.stream()
                .map(dataMap -> ((Number) dataMap.get("num")).intValue())
                .toList();
        return new ClazzAnalysis(clazzList, dataList);
    }
}
