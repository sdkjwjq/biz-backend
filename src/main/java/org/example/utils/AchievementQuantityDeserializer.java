package org.example.utils;

import com.fasterxml.jackson.core.JsonParser;
import com.fasterxml.jackson.core.JsonToken;
import com.fasterxml.jackson.databind.DeserializationContext;
import com.fasterxml.jackson.databind.JsonDeserializer;
import com.fasterxml.jackson.databind.JsonMappingException;

import java.io.IOException;
import java.math.BigDecimal;

/** 仅用于成果奖项数量，避免 JSON 小数被默认整数转换静默截断。 */
public class AchievementQuantityDeserializer extends JsonDeserializer<Integer> {
    @Override
    public Integer deserialize(JsonParser parser, DeserializationContext context) throws IOException {
        JsonToken token = parser.currentToken();
        if (token == JsonToken.VALUE_NUMBER_INT || token == JsonToken.VALUE_NUMBER_FLOAT
                || token == JsonToken.VALUE_STRING) {
            try {
                int value = new BigDecimal(parser.getText().trim()).intValueExact();
                if (value >= 0) return value;
            } catch (NumberFormatException | ArithmeticException ignored) {
                // 非整数、溢出及非法数字统一返回字段级提示。
            }
        }
        throw new InvalidQuantityException(parser);
    }

    public static class InvalidQuantityException extends JsonMappingException {
        public InvalidQuantityException(JsonParser parser) throws IOException {
            super(parser, "成果奖项数量（" + parser.currentName() + "）必须为 0～2147483647 的整数");
        }
    }
}
