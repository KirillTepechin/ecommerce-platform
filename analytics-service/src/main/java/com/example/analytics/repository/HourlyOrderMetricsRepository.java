package com.example.analytics.repository;

import com.example.analytics.model.HourlyOrderMetricsDocument;
import org.springframework.data.mongodb.repository.MongoRepository;

import java.time.Instant;
import java.util.Optional;

public interface HourlyOrderMetricsRepository extends MongoRepository<HourlyOrderMetricsDocument, String> {
    Optional<HourlyOrderMetricsDocument> findByWindowStartAndWindowEnd(Instant windowStart, Instant windowEnd);
}
