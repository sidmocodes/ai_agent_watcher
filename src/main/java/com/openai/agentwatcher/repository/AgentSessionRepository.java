package com.openai.agentwatcher.repository;

import com.openai.agentwatcher.model.AgentSession;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import java.util.List;
import java.util.Optional;

@Repository
public interface AgentSessionRepository extends JpaRepository<AgentSession, Long> {
    
    Optional<AgentSession> findBySessionId(String sessionId);
    
    List<AgentSession> findByAgentId(String agentId);
    
    List<AgentSession> findBySessionStatus(String sessionStatus);
    
    List<AgentSession> findByAgentIdOrderByStartTimeDesc(String agentId);
}
