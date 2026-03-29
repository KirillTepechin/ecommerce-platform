package com.example.analytics.service;

import com.example.analytics.model.HourlyOrderMetricsDocument;
import com.example.analytics.repository.HourlyOrderMetricsRepository;
import com.example.analytics.stream.OrderAnalyticsStream.HourlyOrderMetrics;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.time.Instant;

@Service
@RequiredArgsConstructor
public class AnalyticsMetricsStore {

    private final HourlyOrderMetricsRepository repository;

    public void upsert(Instant windowStart, Instant windowEnd, HourlyOrderMetrics metrics) {
        HourlyOrderMetricsDocument document = repository
                .findByWindowStartAndWindowEnd(windowStart, windowEnd)
                .orElse(HourlyOrderMetricsDocument.builder()
                        .windowStart(windowStart)
                        .windowEnd(windowEnd)
                        .build());

        document.setTotalSales(metrics.getTotalSales());
        document.setAvgOrderValue(metrics.getAvgOrderValue());
        document.setOrdersCount(metrics.getOrdersCount());
        document.setUpdatedAt(Instant.now());

        repository.save(document);
    }
}
