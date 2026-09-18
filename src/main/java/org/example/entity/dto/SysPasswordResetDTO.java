package org.example.entity.dto;

import com.fasterxml.jackson.databind.JsonNode;
import lombok.Data;
import lombok.ToString;

/** 保留 JSON 类型用于严格校验；调试日志不输出凭据。 */
@Data
@ToString(onlyExplicitlyIncluded = true)
public class SysPasswordResetDTO {
    private JsonNode user_id;
    private JsonNode old_password;
    private JsonNode new_password;
}
