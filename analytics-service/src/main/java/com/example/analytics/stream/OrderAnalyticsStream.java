package com.example.analytics.stream;

import com.example.analytics.service.AnalyticsMetricsStore;
import com.fasterxml.jackson.databind.ObjectMapper;
import event.InventoryReservedEvent;
import lombok.Builder;
import lombok.Data;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.apache.kafka.common.serialization.Serdes;
import org.apache.kafka.streams.StreamsBuilder;
import org.apache.kafka.streams.kstream.Consumed;
import org.apache.kafka.streams.kstream.Grouped;
import org.apache.kafka.streams.kstream.Materialized;
import org.apache.kafka.streams.kstream.TimeWindows;
import org.apache.kafka.streams.kstream.Windowed;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.kafka.support.serializer.JsonSerde;

import java.math.BigDecimal;
import java.time.Duration;

@Slf4j
@Configuration
@RequiredArgsConstructor
public class OrderAnalyticsStream {

    private final ObjectMapper objectMapper;
    private final AnalyticsMetricsStore metricsStore;

    @Bean
    public org.apache.kafka.streams.kstream.KStream<String, InventoryReservedEvent> orderMetricsStream(StreamsBuilder builder) {
        JsonSerde<InventoryReservedEvent> eventSerde = new JsonSerde<>(InventoryReservedEvent.class, objectMapper);
        JsonSerde<HourlyOrderMetrics> metricsSerde = new JsonSerde<>(HourlyOrderMetrics.class, objectMapper);

        var stream = builder.stream(
                "inventory-reserved",
                Consumed.with(Serdes.String(), eventSerde)
        );

        stream
                .groupBy((key, event) -> "all-orders", Grouped.with(Serdes.String(), eventSerde))
                .windowedBy(TimeWindows.ofSizeWithNoGrace(Duration.ofHours(1)))
                .aggregate(
                        HourlyOrderMetrics::empty,
                        (aggKey, event, aggregate) -> aggregate.addOrder(event.getTotalAmount()),
                        Materialized.with(Serdes.String(), metricsSerde)
                )
                .toStream()
                .peek((Windowed<String> window, HourlyOrderMetrics metrics) -> {
                            metricsStore.upsert(
                                    window.window().startTime(),
                                    window.window().endTime(),
                                    metrics
                            );
                            log.info("Hourly metrics [{} - {}]: totalSales={}, avgOrderValue={}, orders={} ",
                                    window.window().startTime(),
                                    window.window().endTime(),
                                    metrics.getTotalSales(),
                                    metrics.getAvgOrderValue(),
                                    metrics.getOrdersCount());
                        }
                );

        return stream;
    }

    @Data
    @Builder
    public static class HourlyOrderMetrics {
        private BigDecimal totalSales;
        private BigDecimal avgOrderValue;
        private long ordersCount;

        public static HourlyOrderMetrics empty() {
            return HourlyOrderMetrics.builder()
                    .totalSales(BigDecimal.ZERO)
                    .avgOrderValue(BigDecimal.ZERO)
                    .ordersCount(0)
                    .build();
        }

        public HourlyOrderMetrics addOrder(BigDecimal orderAmount) {
            BigDecimal newTotalSales = totalSales.add(orderAmount);
            long newOrdersCount = ordersCount + 1;
            BigDecimal newAverage = newTotalSales.divide(BigDecimal.valueOf(newOrdersCount), 2, java.math.RoundingMode.HALF_UP);

            return HourlyOrderMetrics.builder()
                    .totalSales(newTotalSales)
                    .avgOrderValue(newAverage)
                    .ordersCount(newOrdersCount)
                    .build();
        }
    }
}
