package com.openai.agentwatcher.config;

import io.micrometer.core.instrument.MeterRegistry;
import io.micrometer.core.instrument.Tag;
import io.micrometer.core.instrument.Tags;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.actuate.metrics.MetricsEndpoint;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import java.util.Arrays;

/**
 * Configuration for custom telemetry and metrics
 */
@Configuration
public class TelemetryConfig {

    @Value("${spring.application.name:agent-watcher}")
    private String applicationName;

    @Bean
    public Tags commonTags() {
        return Tags.of(
                Tag.of("application", applicationName),
                Tag.of("environment", System.getProperty("env", "development"))
        );
    }
}
