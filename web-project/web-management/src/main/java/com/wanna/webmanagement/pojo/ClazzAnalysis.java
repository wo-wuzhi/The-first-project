package com.wanna.webmanagement.pojo;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

/**
 * 班级人数统计结果。
 * 对应接口 /report/studentCountData 的响应结构：两个数组一一对应。
 */
@Data
@AllArgsConstructor
@NoArgsConstructor
public class ClazzAnalysis {
    private List<String> clazzList; //班级名称
    private List<Integer> dataList; //每个班级的人数
}
