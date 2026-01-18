package com.openai.agentwatcher.model;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import jakarta.persistence.*;
import java.time.LocalDateTime;

/**
 * Entity representing an Agent Session
 * Groups related thoughts and actions together
 */
@Entity
@Table(name = "agent_sessions")
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class AgentSession {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "session_id", unique = true, nullable = false)
    private String sessionId;

    @Column(name = "agent_id", nullable = false)
    private String agentId;

    @Column(name = "user_query", columnDefinition = "TEXT")
    private String userQuery;

    @Column(name = "session_status")
    private String sessionStatus; // ACTIVE, COMPLETED, FAILED, TIMEOUT

    @Column(name = "start_time")
    private LocalDateTime startTime;

    @Column(name = "end_time")
    private LocalDateTime endTime;

    @Column(name = "total_thoughts")
    private Integer totalThoughts;

    @Column(name = "total_actions")
    private Integer totalActions;

    @Column(name = "final_response", columnDefinition = "TEXT")
    private String finalResponse;

    @PrePersist
    protected void onCreate() {
        if (startTime == null) {
            startTime = LocalDateTime.now();
        }
        if (sessionStatus == null) {
            sessionStatus = "ACTIVE";
        }
        if (totalThoughts == null) {
            totalThoughts = 0;
        }
        if (totalActions == null) {
            totalActions = 0;
        }
    }

    public void incrementThoughts() {
        this.totalThoughts++;
    }

    public void incrementActions() {
        this.totalActions++;
    }

    public void complete(String finalResponse) {
        this.endTime = LocalDateTime.now();
        this.sessionStatus = "COMPLETED";
        this.finalResponse = finalResponse;
    }
}
