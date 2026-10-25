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
        String port=System.getenv().getOrDefault("SHUANGGAO_UI_BACKEND_PORT","18080");
        if(!port.matches("[0-9]+")||Integer.parseInt(port)<1024||Integer.parseInt(port)>65535)throw new IllegalStateException("Invalid UI audit port");
        System.setProperty("server.port", port);
        System.setProperty("logging.file.name", "logs/ui-audit.log");
        System.setProperty("logging.level.root", "WARN");
        if("1".equals(System.getenv("SHUANGGAO_PERF_METRICS")))System.setProperty("isolated.performance.probe","true");
        SpringApplication app = new SpringApplication(BizApplication.class,
                ReviewBatchRegressionApiTest.NoAutomaticSchedules.class,PerformanceProbeConfig.class);
        app.run(args);
    }
}
