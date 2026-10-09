package com.wanna.webmanagement.config;

import com.aliyun.sdk.service.oss2.OSSClient;
import com.aliyun.sdk.service.oss2.OSSClientBuilder;
import com.aliyun.sdk.service.oss2.credentials.StaticCredentialsProvider;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.util.StringUtils;

/**
 * 创建并托管 OSSClient（单例，全应用共用一个）。
 */
@Slf4j
@Configuration
@RequiredArgsConstructor
public class OssConfig {

    private final OssProperties properties;

    @Bean(destroyMethod = "close")
    public OSSClient ossClient() {
        if (!StringUtils.hasText(properties.getAccessKeyId())
                || !StringUtils.hasText(properties.getAccessKeySecret())) {
            throw new IllegalStateException(
                    "OSS 凭证未配置：请填写 application.yaml 中的 aliyun.oss.access-key-id / access-key-secret");
        }

        OSSClientBuilder builder = OSSClient.newBuilder()
                .credentialsProvider(new StaticCredentialsProvider(
                        properties.getAccessKeyId(),
                        properties.getAccessKeySecret()))
                .region(properties.getRegion());

        // 只有显式配置了 endpoint 才传入，否则交给 SDK 按 region 推导
        if (StringUtils.hasText(properties.getEndpoint())) {
            builder.endpoint(properties.getEndpoint());
        }
        // 自定义域名才需要开 CNAME
        if (Boolean.TRUE.equals(properties.getUseCName())) {
            builder.useCName(true);
        }

        log.info("初始化 OSSClient: region={}, bucket={}, endpoint={}, useCName={}",
                properties.getRegion(), properties.getBucketName(),
                properties.getEndpoint(), properties.getUseCName());
        return builder.build();
    }
}
