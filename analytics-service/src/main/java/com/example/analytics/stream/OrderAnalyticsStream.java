package com.example.analytics.stream;

import com.example.analytics.service.AnalyticsMetricsStore;
import event.InventoryReservedEvent;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.RequiredArgsConstructor;
import lombok.experimental.Accessors;
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

    private final AnalyticsMetricsStore metricsStore;

    @Bean
    public org.apache.kafka.streams.kstream.KStream<String, InventoryReservedEvent> orderMetricsStream(  StreamsBuilder builder) {
        final JsonSerde<InventoryReservedEvent> eventSerde = new JsonSerde<>(InventoryReservedEvent.class);
        final JsonSerde<HourlyOrderMetrics> metricsSerde = new JsonSerde<>(HourlyOrderMetrics.class);

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
    @NoArgsConstructor
    @Accessors(chain = true)
    public static class HourlyOrderMetrics {
        private BigDecimal totalSales;
        private BigDecimal avgOrderValue;
        private long ordersCount;


        public static HourlyOrderMetrics empty() {
            return new HourlyOrderMetrics()
                    .setTotalSales(BigDecimal.ZERO)
                    .setAvgOrderValue(BigDecimal.ZERO)
                    .setOrdersCount(0);
        }

        public HourlyOrderMetrics addOrder(BigDecimal orderAmount) {
            final BigDecimal newTotalSales = totalSales.add(orderAmount);
            final long newOrdersCount = ordersCount + 1;
            final BigDecimal newAverage = newTotalSales.divide(BigDecimal.valueOf(newOrdersCount), 2, java.math.RoundingMode.HALF_UP);

            return new HourlyOrderMetrics()
                    .setTotalSales(newTotalSales)
                    .setAvgOrderValue(newAverage)
                    .setOrdersCount(newOrdersCount);
        }
    }
}
