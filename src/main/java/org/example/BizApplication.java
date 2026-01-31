package org.example;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.scheduling.annotation.EnableScheduling;

/**
 * 业务应用主启动类
 */
@SpringBootApplication
@EnableScheduling  // 启用定时任务支持
public class BizApplication {
    /**
     * 应用入口方法
     * @param args 命令行参数
     */
    public static void main(String[] args) {
        SpringApplication.run(BizApplication.class, args);
    }
}