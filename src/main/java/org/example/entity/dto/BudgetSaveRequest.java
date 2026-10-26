package org.example.entity.dto;

import com.fasterxml.jackson.annotation.JsonValue;
import com.fasterxml.jackson.core.JsonParser;
import com.fasterxml.jackson.databind.DeserializationContext;
import com.fasterxml.jackson.databind.DeserializationFeature;
import com.fasterxml.jackson.databind.JsonDeserializer;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.annotation.JsonDeserialize;
import java.io.IOException;

/** Keep amounts decimal-exact without changing deserialization of existing APIs. */
@JsonDeserialize(using = BudgetSaveRequest.Deserializer.class)
public record BudgetSaveRequest(JsonNode changes) {
    @JsonValue
    public JsonNode changes() { return changes; }

    public static final class Deserializer extends JsonDeserializer<BudgetSaveRequest> {
        private static final ObjectMapper DECIMALS = new ObjectMapper()
                .enable(DeserializationFeature.USE_BIG_DECIMAL_FOR_FLOATS);
        @Override
        public BudgetSaveRequest deserialize(JsonParser parser, DeserializationContext context) throws IOException {
            return new BudgetSaveRequest(DECIMALS.readTree(parser));
        }
    }
}
