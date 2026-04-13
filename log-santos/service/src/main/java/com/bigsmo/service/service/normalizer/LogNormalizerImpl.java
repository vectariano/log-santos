package com.bigsmo.service.normalizer;

import com.bigsmo.common.dto.NormalizedLogEvent;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.time.Instant;
import java.util.HashMap;
import java.util.Map;

@Service
@RequiredArgsConstructor
public class LogNormalizerImpl implements LogNormalizer {

    private final ObjectMapper objectMapper;

    @Override
    public NormalizedLogEvent normalize(String rawMessage) {
        try {
            JsonNode root = objectMapper.readTree(rawMessage);

            String serviceId = getText(root, "service_id", "unknown-service");
            String timestamp = getText(root, "@timestamp", Instant.now().toString());
            String level = getText(root, "level", "INFO").toUpperCase();
            String message = getText(root, "message", "");
            String traceId = getNullableText(root, "trace_id");
            String spanId = getNullableText(root, "span_id");

            Map<String, Object> attrs = extractAttrs(root);

            return NormalizedLogEvent.builder()
                    .serviceId(serviceId)
                    .timestamp(timestamp)
                    .level(level)
                    .message(message)
                    .traceId(traceId)
                    .spanId(spanId)
                    .attrs(attrs.isEmpty() ? null : attrs)
                    .build();

        } catch (Exception e) {
            return NormalizedLogEvent.builder()
                    .serviceId("unknown-service")
                    .timestamp(Instant.now().toString())
                    .level("ERROR")
                    .message("Failed to normalize raw log")
                    .attrs(Map.of(
                            "raw_message", rawMessage,
                            "error", e.getMessage()
                    ))
                    .build();
        }
    }

    private String getText(JsonNode node, String fieldName, String defaultValue) {
        JsonNode value = node.get(fieldName);
        return value != null && !value.isNull() ? value.asText() : defaultValue;
    }

    private String getNullableText(JsonNode node, String fieldName) {
        JsonNode value = node.get(fieldName);
        return value != null && !value.isNull() ? value.asText() : null;
    }

    private Map<String, Object> extractAttrs(JsonNode root) {
        Map<String, Object> attrs = new HashMap<>();
        JsonNode attrsNode = root.get("attrs");

        if (attrsNode != null && attrsNode.isObject()) {
            attrsNode.fields().forEachRemaining(entry -> {
                JsonNode value = entry.getValue();
                attrs.put(entry.getKey(), convertNode(value));
            });
        }

        return attrs;
    }

    private Object convertNode(JsonNode node) {
        if (node.isTextual()) return node.asText();
        if (node.isInt()) return node.asInt();
        if (node.isLong()) return node.asLong();
        if (node.isDouble() || node.isFloat() || node.isBigDecimal()) return node.asDouble();
        if (node.isBoolean()) return node.asBoolean();
        return node.toString();
    }
}