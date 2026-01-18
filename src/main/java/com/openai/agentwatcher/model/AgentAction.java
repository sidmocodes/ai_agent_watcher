package com.openai.agentwatcher.model;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import jakarta.persistence.*;
import java.time.LocalDateTime;

/**
 * Entity representing an Agent Action or operation
 * Captures what the agent is doing during execution
 */
@Entity
@Table(name = "agent_actions")
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class AgentAction {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "agent_id", nullable = false)
    private String agentId;

    @Column(name = "session_id", nullable = false)
    private String sessionId;

    @Column(name = "action_type")
    private String actionType; // API_CALL, TOOL_USE, COMPUTATION, DATA_RETRIEVAL

    @Column(name = "action_name")
    private String actionName;

    @Column(name = "input_data", columnDefinition = "TEXT")
    private String inputData;

    @Column(name = "output_data", columnDefinition = "TEXT")
    private String outputData;

    @Column(name = "status")
    private String status; // STARTED, IN_PROGRESS, COMPLETED, FAILED

    @Column(name = "start_time")
    private LocalDateTime startTime;

    @Column(name = "end_time")
    private LocalDateTime endTime;

    @Column(name = "duration_ms")
    private Long durationMs;

    @Column(name = "error_message", columnDefinition = "TEXT")
    private String errorMessage;

    @Column(name = "related_thought_id")
    private Long relatedThoughtId;

    @PrePersist
    protected void onCreate() {
        if (startTime == null) {
            startTime = LocalDateTime.now();
        }
        if (status == null) {
            status = "STARTED";
        }
    }

    public void complete() {
        this.endTime = LocalDateTime.now();
        this.status = "COMPLETED";
        if (startTime != null && endTime != null) {
            this.durationMs = java.time.Duration.between(startTime, endTime).toMillis();
        }
    }

    public void fail(String errorMessage) {
        this.endTime = LocalDateTime.now();
        this.status = "FAILED";
        this.errorMessage = errorMessage;
        if (startTime != null && endTime != null) {
            this.durationMs = java.time.Duration.between(startTime, endTime).toMillis();
        }
    }
}
