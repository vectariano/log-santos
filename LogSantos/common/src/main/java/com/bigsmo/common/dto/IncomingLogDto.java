package com.bigsmo.common.dto;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonProperty;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.Map;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@JsonIgnoreProperties(ignoreUnknown = true)
public class IncomingLogDto {
    @NotBlank(message = "service_id is required")
    @Size(min = 1, max = 256, message = "...")
    @JsonProperty("service_id")
    private String serviceId;

    @NotNull(message = "@timestamp is required")
    @JsonProperty("@timestamp")
    private String timestamp;

    @NotBlank(message = "level is required")
    @Size(min = 1, max = 32, message = "...")
    private String level;

    @NotBlank(message = "message is required")
    @Size(max = 1048576, message = "message must not exceed 1MB")
    private String message;

    @Size(max = 128, message = "trace_id must not exceed 128 characters")
    @JsonProperty("trace_id")
    private String traceId;

    @Size(max = 128, message = "span_id must not exceed 128 characters")
    @JsonProperty("span_id")
    private String spanId;

    @Size(max = 100, message = "attrs must not have more than 100 keys")
    private Map<String, Object> attrs;

    public void validate() {
        if (serviceId != null) {
            serviceId = serviceId.trim();
        }

        if (traceId != null && traceId.isBlank()) {
            traceId = null;
        }

        if (spanId != null && spanId.isBlank()) {
            spanId = null;
        }
    }
}