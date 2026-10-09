package com.wanna.webmanagement.config;

import lombok.Data;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.stereotype.Component;

/**
 * 阿里云 OSS 配置项，对应 application.yaml 里的 aliyun.oss.*
 */
@Data
@Component
@ConfigurationProperties(prefix = "aliyun.oss")
public class OssProperties {

    /** 地域 ID，如 cn-beijing（注意不带 oss- 前缀） */
    private String region;

    /** 访问域名。留空时 SDK 会按 region 自动推导默认域名 */
    private String endpoint;

    /** Bucket 名称 */
    private String bucketName;

    /** AccessKeyId（建议走环境变量注入，不要提交到 Git） */
    private String accessKeyId;

    /** AccessKeySecret */
    private String accessKeySecret;

    /** 是否使用自定义域名（CNAME）。用默认 OSS 域名时必须为 false */
    private Boolean useCName = false;
}
