package org.example.controller;

import com.fasterxml.jackson.databind.JsonNode;
import jakarta.servlet.http.HttpServletRequest;
import org.example.entity.vo.ErrorVO;
import org.example.service.WorkRecordException;
import org.example.service.WorkRecordService;
import org.example.service.WorkRecordExportService;
import org.example.utils.JWTUtil;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.ResponseEntity;
import org.springframework.http.converter.HttpMessageNotReadableException;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.method.annotation.MethodArgumentTypeMismatchException;

/** 独立纪实接口；所有身份来自已验证的登录 Token。 */
@RestController
@RequestMapping("/work-records")
public class WorkRecordController {
    private static final Logger LOG = LoggerFactory.getLogger(WorkRecordController.class);
    private final WorkRecordService records;
    private final WorkRecordExportService exporter;
    public WorkRecordController(WorkRecordService records, WorkRecordExportService exporter) { this.records = records; this.exporter = exporter; }

    @PostMapping("/export")
    public ResponseEntity<byte[]> export(HttpServletRequest request, @RequestBody JsonNode body) throws java.io.IOException {
        JsonNode selected = body == null ? null : body.get("ids");
        if (selected == null || !selected.isArray() || selected.isEmpty()) throw new WorkRecordException(400,"请选择需要导出的纪实");
        java.util.List<Long> ids = new java.util.ArrayList<>();
        for (JsonNode id : selected) {
            if (!id.isIntegralNumber() || !id.canConvertToLong() || id.asLong()<=0) throw new WorkRecordException(400,"纪实编号必须为正整数");
            ids.add(id.asLong());
        }
        var details = records.exportDetails(user(request), ids);
        byte[] bytes = exporter.render(details);
        String filename = details.size()==1 ? "工作纪实-"+details.get(0).record().getRecordYear()+"-"+details.get(0).record().getRecordMonth()+".docx" : "工作纪实-合并导出.docx";
        return ResponseEntity.ok().header("Content-Disposition", org.springframework.http.ContentDisposition.attachment()
                .filename(filename,java.nio.charset.StandardCharsets.UTF_8).build().toString())
                .header("Cache-Control","no-store")
                .contentType(org.springframework.http.MediaType.parseMediaType("application/vnd.openxmlformats-officedocument.wordprocessingml.document"))
                .body(bytes);
    }

    private Long user(HttpServletRequest request) {
        return JWTUtil.getUserIdFromToken(request.getHeader("Authorization"));
    }

    @GetMapping("/capabilities")
    public Object capabilities(HttpServletRequest request) { return records.capabilities(user(request)); }

    @GetMapping("/authors")
    public Object authors(HttpServletRequest request) { return records.authors(user(request)); }

    @PostMapping("/{id}/delete")
    public Object delete(HttpServletRequest request, @PathVariable Long id, @RequestBody JsonNode body) {
        return records.delete(user(request), id, body);
    }

    @PostMapping("/{id}/save")
    public Object save(HttpServletRequest request, @PathVariable Long id, @RequestBody JsonNode body) {
        return records.save(user(request), id, body, false);
    }

    @PostMapping("/{id}/submit")
    public Object submit(HttpServletRequest request, @PathVariable Long id, @RequestBody JsonNode body) {
        return records.save(user(request), id, body, true);
    }

    @GetMapping("/statistics")
    public Object statistics(HttpServletRequest request, @RequestParam int year, @RequestParam int month) {
        return records.preview(user(request), year, month);
    }

    @GetMapping
    public Object list(HttpServletRequest request, @RequestParam(required=false) Integer year,
                       @RequestParam(required=false) Integer month, @RequestParam(required=false) Long ownerId,
                       @RequestParam(required=false) Integer status, @RequestParam(defaultValue="1") int page,
                       @RequestParam(defaultValue="20") int pageSize) {
        return records.list(user(request), year, month, ownerId, status, page, pageSize);
    }

    @GetMapping("/{id}")
    public Object detail(HttpServletRequest request, @PathVariable Long id) { return records.detail(user(request), id); }

    @PostMapping
    public Object create(HttpServletRequest request, @RequestBody JsonNode body) {
        return records.create(user(request), integer(body, "year"), integer(body, "month"));
    }

    @PostMapping("/{id}/statistics/refresh")
    public Object refresh(HttpServletRequest request, @PathVariable Long id, @RequestBody JsonNode body) {
        JsonNode version = body == null ? null : body.get("version");
        if (version == null || !version.isIntegralNumber() || !version.canConvertToLong() || version.asLong() < 0) {
            throw new WorkRecordException(400, "请提供有效的纪实版本号");
        }
        return records.refresh(user(request), id, version.asLong());
    }

    private int integer(JsonNode body, String field) {
        JsonNode value = body == null ? null : body.get(field);
        if (value == null || !value.isIntegralNumber() || !value.canConvertToInt()) throw new WorkRecordException(400, "请提供整数年份和月份");
        return value.asInt();
    }

    @ExceptionHandler(WorkRecordException.class)
    public ResponseEntity<ErrorVO> businessError(WorkRecordException error) {
        return ResponseEntity.status(error.getCode()).body(new ErrorVO(error.getMessage(), error.getCode()));
    }

    @ExceptionHandler({HttpMessageNotReadableException.class, MethodArgumentTypeMismatchException.class,
            org.springframework.web.bind.MissingServletRequestParameterException.class})
    public ResponseEntity<ErrorVO> parameterError(Exception error) {
        return ResponseEntity.badRequest().body(new ErrorVO("请求参数不正确", 400));
    }

    @ExceptionHandler(Exception.class)
    public ResponseEntity<ErrorVO> unexpected(Exception error) {
        LOG.error("工作纪实操作失败", error);
        return ResponseEntity.internalServerError().body(new ErrorVO("工作纪实操作失败，请稍后重试", 500));
    }
}
