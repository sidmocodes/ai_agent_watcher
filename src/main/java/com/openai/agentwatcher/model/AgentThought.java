package com.openai.agentwatcher.model;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import jakarta.persistence.*;
import java.time.LocalDateTime;
import java.util.List;

/**
 * Entity representing an Agent Thought process
 * Captures what the agent is thinking during computation
 */
@Entity
@Table(name = "agent_thoughts")
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class AgentThought {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "agent_id", nullable = false)
    private String agentId;

    @Column(name = "session_id", nullable = false)
    private String sessionId;

    @Column(name = "thought_type")
    private String thoughtType; // PLANNING, REASONING, TOOL_SELECTION, EXECUTION, REFLECTION

    @Column(name = "thought_content", columnDefinition = "TEXT")
    private String thoughtContent;

    @Column(name = "confidence_score")
    private Double confidenceScore;

    @Column(name = "timestamp")
    private LocalDateTime timestamp;

    @Column(name = "parent_thought_id")
    private Long parentThoughtId;

    @Column(name = "metadata", columnDefinition = "TEXT")
    private String metadata; // JSON string for additional data

    @PrePersist
    protected void onCreate() {
        timestamp = LocalDateTime.now();
    }
}
