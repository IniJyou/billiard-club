package com.club;

import org.mybatis.spring.annotation.MapperScan;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

/**
 * 台球厅会员管理系统 启动类
 */
@SpringBootApplication
@MapperScan("com.club.mapper")
public class BilliardClubApplication {

    public static void main(String[] args) {
        SpringApplication.run(BilliardClubApplication.class, args);
    }
}
