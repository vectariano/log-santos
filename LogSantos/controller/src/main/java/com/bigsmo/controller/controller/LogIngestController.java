package com.bigsmo.controller.controller;

import com.bigsmo.common.dto.BatchResponse;
import com.bigsmo.common.dto.IncomingLogDto;
import com.bigsmo.common.dto.LogBatchRequest;
import com.bigsmo.controller.service.LogIngestService;
import com.bigsmo.controller.service.TokenValidator;

import jakarta.servlet.http.HttpServletRequest;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import tools.jackson.databind.ObjectMapper;

import java.util.List;
import java.util.Map;

@Slf4j
@RestController
@RequiredArgsConstructor
@RequestMapping("/api/v1/logs")
public class LogIngestController {

    private final TokenValidator tokenValidator;
    private final LogIngestService logIngestService;
    private final ObjectMapper objectMapper;

    @PostMapping("/batch")
    public ResponseEntity<?> ingestBatch(
            @RequestHeader(value = "Authorization", required = false) String authHeader,
            @Valid @RequestBody LogBatchRequest request,
            HttpServletRequest httpRequest) {
        String clientIp = httpRequest.getRemoteAddr();

        // 1. Проверка токена
        if (!tokenValidator.isValid(authHeader)) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED)
                    .body(Map.of("error", "UNAUTHORIZED", "message", "Invalid or missing agent token."));
        }

        try {
            // 2. делаем строки из классов !напрягает
            List<String> backToString = request.getLogs().stream()
                    .map(this::toJson)
                    .toList();

            // 3. Запись в raw-logs topic
            LogIngestService.IngestResult result = logIngestService.ingest(backToString);
            log.info("the thing ingested: {}", result.toString());

            BatchResponse response = BatchResponse.builder()
                    .status(result.accepted() > 0 ? "accepted" : "rejected")
                    .acceptedCount(result.accepted())
                    .rejectedCount(result.dropped())
                    .build();

            return ResponseEntity.status(HttpStatus.ACCEPTED).body(response);

        } catch (Exception e) {
            log.error("Ingest pipeline failed | IP: {}", clientIp, e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(Map.of("error", "PROCESSING_FAILED", "message", e.getMessage()));
        }
    }

    @GetMapping("/health")
    public ResponseEntity<Map<String, String>> health() {
        return ResponseEntity.ok(Map.of("status", "UP", "component", "log-ingest-api"));
    }

    private String toJson(IncomingLogDto dto) {
        return objectMapper.writeValueAsString(dto);
    }
}