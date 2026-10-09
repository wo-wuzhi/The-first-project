package com.wanna.webmanagement.util;

import com.aliyun.sdk.service.oss2.OSSClient;
import com.aliyun.sdk.service.oss2.models.PutObjectRequest;
import com.aliyun.sdk.service.oss2.models.PutObjectResult;
import com.aliyun.sdk.service.oss2.transport.BinaryData;
import com.wanna.webmanagement.config.OssProperties;
import com.wanna.webmanagement.exception.BusinessException;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.io.InputStream;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.UUID;

/**
 * OSS 上传工具。
 */
@Slf4j
@Component
@RequiredArgsConstructor
public class OssUtil {

    private final OSSClient ossClient;
    private final OssProperties properties;

    /**
     * 上传文件到 OSS，返回可直接访问的 URL。
     */
    public String upload(MultipartFile file) {
        if (file == null || file.isEmpty()) {
            throw new BusinessException("上传文件不能为空");
        }

        String objectKey = buildObjectKey(file.getOriginalFilename());

        try (InputStream in = file.getInputStream()) {
            PutObjectRequest request = PutObjectRequest.newBuilder()
                    .bucket(properties.getBucketName())
                    .key(objectKey)
                    .contentType(file.getContentType())
                    .body(BinaryData.fromStream(in))
                    .build();

            PutObjectResult result = ossClient.putObject(request);
            log.info("OSS 上传成功: key={}, eTag={}", objectKey, result.eTag());
        } catch (IOException e) {
            log.error("OSS 上传失败: key={}", objectKey, e);
            throw new BusinessException("文件上传失败: " + e.getMessage());
        }

        return buildUrl(objectKey);
    }

    /**
     * 生成对象名：yyyy/MM/dd/UUID.扩展名
     * UUID 保证唯一（避免同名覆盖），日期目录避免单目录文件过多。
     */
    private String buildObjectKey(String originalFilename) {
        String ext = "";
        if (originalFilename != null) {
            int dot = originalFilename.lastIndexOf('.');
            // 没有扩展名时 dot 为 -1，直接跳过，避免 substring(-1) 抛异常
            if (dot >= 0) {
                ext = originalFilename.substring(dot).toLowerCase();
            }
        }
        String datePath = LocalDate.now().format(DateTimeFormatter.ofPattern("yyyy/MM/dd"));
        return datePath + "/" + UUID.randomUUID() + ext;
    }

    /**
     * 拼出可访问的 URL：
     * 默认域名   https://{bucket}.{endpoint}/{key}
     * 自定义域名 https://{endpoint}/{key}
     */
    private String buildUrl(String objectKey) {
        String endpoint = properties.getEndpoint();
        if (endpoint == null || endpoint.isBlank()) {
            endpoint = "oss-" + properties.getRegion() + ".aliyuncs.com";
        }
        endpoint = endpoint.replaceFirst("^https?://", "");

        if (Boolean.TRUE.equals(properties.getUseCName())) {
            return "https://" + endpoint + "/" + objectKey;
        }
        return "https://" + properties.getBucketName() + "." + endpoint + "/" + objectKey;
    }
}
