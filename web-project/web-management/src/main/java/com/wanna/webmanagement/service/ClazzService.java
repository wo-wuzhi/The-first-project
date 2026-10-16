package com.wanna.webmanagement.service;

import com.wanna.webmanagement.pojo.Clazz;
import com.wanna.webmanagement.pojo.PageResult;

import java.time.LocalDate;
import java.util.List;

public interface ClazzService {
    PageResult<Clazz> pageShow(Integer page, Integer pageSize, String name, LocalDate begin, LocalDate end);

    void delete(Integer id);

    void save(Clazz clazz);

    Clazz getById(Integer id);

    void update(Clazz clazz);

    List<Clazz> findAll();

}
