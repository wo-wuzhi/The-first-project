package com.wanna.webmanagement.service;

import com.wanna.webmanagement.pojo.ClazzAnalysis;
import com.wanna.webmanagement.pojo.JobAnalysis;

import java.util.List;
import java.util.Map;

public interface ReportService {

    JobAnalysis getEmpJobData();

    List<Map<String, Object>> getEmpGenderData();

    List<Map<String, Object>> getStudentDegreeData();

    ClazzAnalysis getStudentCountData();
}
