package com.openai.agentwatcher.repository;

import com.openai.agentwatcher.model.AgentThought;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import java.util.List;

@Repository
public interface AgentThoughtRepository extends JpaRepository<AgentThought, Long> {
    
    List<AgentThought> findBySessionId(String sessionId);
    
    List<AgentThought> findByAgentId(String agentId);
    
    List<AgentThought> findBySessionIdOrderByTimestampAsc(String sessionId);
    
    List<AgentThought> findByThoughtType(String thoughtType);
}
