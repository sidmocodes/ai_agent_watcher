package com.openai.agentwatcher.model;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import jakarta.persistence.*;
import java.time.LocalDateTime;

/**
 * Entity for capturing telemetry metrics about agent performance
 */
@Entity
@Table(name = "agent_telemetry")
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class AgentTelemetry {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "agent_id", nullable = false)
    private String agentId;

    @Column(name = "session_id", nullable = false)
    private String sessionId;

    @Column(name = "metric_name")
    private String metricName;

    @Column(name = "metric_value")
    private Double metricValue;

    @Column(name = "metric_unit")
    private String metricUnit;

    @Column(name = "metric_type")
    private String metricType; // LATENCY, TOKENS, COST, ERROR_RATE, etc.

    @Column(name = "timestamp")
    private LocalDateTime timestamp;

    @Column(name = "tags", columnDefinition = "TEXT")
    private String tags; // JSON string for additional tags

    @PrePersist
    protected void onCreate() {
        timestamp = LocalDateTime.now();
    }
}
