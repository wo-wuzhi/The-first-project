package com.wanna.webmanagement.controller;

import com.wanna.webmanagement.pojo.Result;
import com.wanna.webmanagement.util.OssUtil;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.multipart.MultipartFile;

@Slf4j
@RestController
@RequiredArgsConstructor
public class UploadController {

    private final OssUtil ossUtil;

    @PostMapping("/upload")
    public Result upload(@RequestParam("file") MultipartFile file) {
        log.info("收到上传请求: {}", file.getOriginalFilename());
        String url = ossUtil.upload(file);
        return Result.success(url);
    }
}
