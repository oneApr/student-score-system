package com.fmk.student.config;

import io.swagger.v3.oas.models.OpenAPI;
import io.swagger.v3.oas.models.info.Contact;
import io.swagger.v3.oas.models.info.Info;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

/**
 * Knife4j / SpringDoc 文档配置
 * 访问地址：http://localhost:8088/fmk/doc.html
 * Token 通过 Knife4j「全局参数」传入：key=Authorization, value=Bearer {token}
 */
@Configuration
public class Knife4jConfig {

    @Bean
    public OpenAPI customOpenAPI() {
        return new OpenAPI()
                .info(new Info()
                        .title("高校学生成绩管理信息系统")
                        .description("基于Spring-Boot课程目标达成情况管理系统")
                        .version("v1.0.0")
                        .contact(new Contact().name("飞马客")));
    }
}
