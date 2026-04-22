package com.bigsmo.service.service;

import com.bigsmo.common.dto.LogMetricEvent;
import com.bigsmo.common.model.NormalizedLogEvent;
import com.bigsmo.service.kafka.KafkaPublisherService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

import java.time.Instant;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.ConcurrentLinkedQueue;

@Slf4j
@Component
@RequiredArgsConstructor
public class LogMetricsBatcher {
    private final ConcurrentLinkedQueue<NormalizedLogEvent> batch = new ConcurrentLinkedQueue<>();
    private final MetricsFormer metricsFormer;
    private final KafkaPublisherService publisher;

    @Value("${app.metrics.batch.interval-ms:3600000}")
    private long batchIntervalMs;

    public void addToBatch(NormalizedLogEvent normalized) {
        batch.add(normalized);
    }

    @Scheduled(fixedRateString = "${app.metrics.batch.interval-ms:3600000}")
    public void formAndPublishMetrics() {
        if (batch.isEmpty()) {
            return;
        }

        List<NormalizedLogEvent> currentBatch = new ArrayList<>();
        NormalizedLogEvent event;
        while ((event = batch.poll()) != null) {
            currentBatch.add(event);
        }

        if (currentBatch.isEmpty()) {
            return;
        }

        try {
            Instant now = Instant.now();
            Instant windowStart = now.minusMillis(batchIntervalMs);

            List<LogMetricEvent> metrics = metricsFormer.formMetrics(
                    currentBatch,
                    windowStart,   // <-- start of this batch window
                    now            // <-- end of this batch window
            );

            publisher.publishMetrics(metrics);

            log.info("Formed and published {} metrics from {} normalized logs (window: {} -> {})",
                    metrics.size(), currentBatch.size(), windowStart, now);

        } catch (Exception e) {
            log.error("Failed to form metrics from batch of {} logs", currentBatch.size(), e);
        }
    }
}