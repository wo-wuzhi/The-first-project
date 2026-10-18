package com.wanna.webmanagement.controller;

import com.wanna.webmanagement.exception.BusinessException;
import com.wanna.webmanagement.pojo.Emp;
import com.wanna.webmanagement.pojo.LoginInfo;
import com.wanna.webmanagement.pojo.Result;
import com.wanna.webmanagement.service.EmpService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@Slf4j
@RestController
@RequestMapping("/login")
public class LoginController {

    @Autowired
    private EmpService  empService;

    @PostMapping
    public Result login(@RequestBody Emp emp){
        log.info("员工登入 {}",emp);
        LoginInfo loginInfo = empService.login(emp);
        if(loginInfo!=null){
            return Result.success(loginInfo);
        }else{
            throw new BusinessException("用户名或密码错误");
        }
    }
}
