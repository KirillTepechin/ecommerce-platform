package com.example.analytics.model;

import lombok.Builder;
import lombok.Data;
import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.index.CompoundIndex;
import org.springframework.data.mongodb.core.mapping.Document;

import java.math.BigDecimal;
import java.time.Instant;

@Data
@Builder
@Document(collection = "hourly_order_metrics")
@CompoundIndex(name = "window_idx", def = "{'windowStart': 1, 'windowEnd': 1}", unique = true)
public class HourlyOrderMetricsDocument {

    @Id
    private String id;

    private Instant windowStart;
    private Instant windowEnd;

    private BigDecimal totalSales;
    private BigDecimal avgOrderValue;
    private long ordersCount;

    private Instant updatedAt;
}
