package org.example.config;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.SerializationFeature;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.converter.json.MappingJackson2HttpMessageConverter;

import java.text.SimpleDateFormat;
import java.util.TimeZone;

/**
 * Jackson全局配置：统一Date类型序列化/反序列化格式
 */
@Configuration
public class JacksonConfig {

    @Bean
    public MappingJackson2HttpMessageConverter mappingJackson2HttpMessageConverter() {
        MappingJackson2HttpMessageConverter converter = new MappingJackson2HttpMessageConverter();
        ObjectMapper objectMapper = new ObjectMapper();

        // 1. 指定Date类型的解析/生成格式（匹配yyyy-MM-dd HH:mm:ss）
        objectMapper.setDateFormat(new SimpleDateFormat("yyyy-MM-dd HH:mm:ss"));
        // 2. 设置时区（避免时间偏移，建议使用东八区）
        objectMapper.setTimeZone(TimeZone.getTimeZone("GMT+8"));
        // 3. 解决序列化时Date类型为时间戳的问题
        objectMapper.disable(SerializationFeature.WRITE_DATES_AS_TIMESTAMPS);
        // 4. 支持Java 8时间类型（可选，兼容LocalDateTime等）
        objectMapper.registerModule(new JavaTimeModule());

        converter.setObjectMapper(objectMapper);
        return converter;
    }
}