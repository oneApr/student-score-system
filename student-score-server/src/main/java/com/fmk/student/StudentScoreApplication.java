package com.fmk.student;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

@SpringBootApplication
public class StudentScoreApplication {
    public static void main(String[] args) {
        SpringApplication.run(StudentScoreApplication.class, args);
        String fmkLogo = """
                \033[36m
                 _______  __   __  ___   _  
                |       ||  |_|  ||   | | | 
                |    ___||       ||   |_| | 
                |   |___ |       ||      _| 
                |    ___||       ||     |_  
                |   |    | ||_|| ||    _  | 
                |___|    |_|   |_||___| |_| 
                \033[0m""";

        System.out.println(fmkLogo);
        // \033[32m 是开启绿色字体
        System.out.println("\033[32m高校学生成绩信息管理系统 启动成功!\033[0m ");
    }
}
