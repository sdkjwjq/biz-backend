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
        org.example.utils.SpreadsheetSupport.configure();
        if (args.length == 3 && args[0].equals("--legacy-review-return-repair")) {
            var app = new SpringApplication(BizApplication.class);
            app.setWebApplicationType(org.springframework.boot.WebApplicationType.NONE);
            app.setDefaultProperties(java.util.Map.of("workflow.maintenance", "true", "spring.main.banner-mode", "off"));
            int result = 0;
            try (var context = app.run()) {
                var repaired = context.getBean(org.example.service.LegacyReviewReturnRepair.class).run(args[1], java.nio.file.Path.of(args[2]));
                System.out.println("LEGACY_REVIEW_RETURN_REPAIR_OK " + repaired);
                if (((Number)repaired.get("blocked")).intValue() > 0) result = 1;
            } catch (Exception error) {
                System.err.println("LEGACY_REVIEW_RETURN_REPAIR_FAILED " + error.getMessage()); result = 1;
            }
            System.exit(result); return;
        }
        if (args.length == 3 && args[0].equals("--audit-workflow-maintenance")) {
            var app = new SpringApplication(BizApplication.class);
            app.setWebApplicationType(org.springframework.boot.WebApplicationType.NONE);
            app.setDefaultProperties(java.util.Map.of("workflow.maintenance", "true", "spring.main.banner-mode", "off"));
            int result=0;
            try (var context=app.run()) {
                var maintenance=context.getBean(org.example.service.AuditWorkflowMaintenance.class);
                var path=java.nio.file.Path.of(args[2]);
                if(args[1].equals("rollback"))maintenance.rollback(path);
                else System.out.println("WORKFLOW_MAINTENANCE_OK "+maintenance.run(args[1],path));
            } catch(Exception error) {
                System.err.println("WORKFLOW_MAINTENANCE_FAILED "+error.getMessage());result=1;
            }
            System.exit(result);
            return;
        }
        SpringApplication.run(BizApplication.class, args);
    }
}
