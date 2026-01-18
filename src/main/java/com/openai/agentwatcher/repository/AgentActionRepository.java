package com.openai.agentwatcher.repository;

import com.openai.agentwatcher.model.AgentAction;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import java.util.List;

@Repository
public interface AgentActionRepository extends JpaRepository<AgentAction, Long> {
    
    List<AgentAction> findBySessionId(String sessionId);
    
    List<AgentAction> findByAgentId(String agentId);
    
    List<AgentAction> findBySessionIdOrderByStartTimeAsc(String sessionId);
    
    List<AgentAction> findByStatus(String status);
    
    List<AgentAction> findByActionType(String actionType);
}
