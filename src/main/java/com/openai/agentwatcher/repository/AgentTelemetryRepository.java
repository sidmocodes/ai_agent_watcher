package com.openai.agentwatcher.repository;

import com.openai.agentwatcher.model.AgentTelemetry;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import java.util.List;

@Repository
public interface AgentTelemetryRepository extends JpaRepository<AgentTelemetry, Long> {
    
    List<AgentTelemetry> findBySessionId(String sessionId);
    
    List<AgentTelemetry> findByAgentId(String agentId);
    
    List<AgentTelemetry> findByMetricType(String metricType);
    
    List<AgentTelemetry> findBySessionIdOrderByTimestampAsc(String sessionId);
}
