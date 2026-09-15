package org.example;

import org.springframework.boot.SpringApplication;

/** 独立浏览器审计入口：仅允许隔离库，复用真实业务服务并关闭自动调度。 */
public class IsolatedUiAuditServer {
    public static void main(String[] args) {
        System.setProperty("spring.devtools.restart.enabled", "false");
        String url = System.getenv("SHUANGGAO_TEST_JDBC_URL");
        if (url == null || !url.matches("jdbc:mysql://127\\.0\\.0\\.1:3306/biz_review_test_[0-9a-f]{32}\\?.*")) {
            throw new IllegalStateException("UI audit requires an isolated schema");
        }
        System.setProperty("spring.datasource.url", url);
        System.setProperty("spring.datasource.username", System.getenv("SHUANGGAO_TEST_DB_USER"));
        System.setProperty("spring.datasource.password", System.getenv("SHUANGGAO_TEST_DB_PASSWORD"));
        System.setProperty("server.address", "127.0.0.1");
        System.setProperty("server.port", "18080");
        System.setProperty("logging.file.name", "logs/ui-audit.log");
        System.setProperty("logging.level.root", "WARN");
        SpringApplication app = new SpringApplication(BizApplication.class,
                ReviewBatchRegressionApiTest.NoAutomaticSchedules.class);
        app.run(args);
    }
}
