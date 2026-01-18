package com.openai.agentwatcher.service;

import com.openai.agentwatcher.model.AgentAction;
import com.openai.agentwatcher.model.AgentSession;
import com.openai.agentwatcher.model.AgentTelemetry;
import com.openai.agentwatcher.model.AgentThought;
import com.openai.agentwatcher.repository.AgentActionRepository;
import com.openai.agentwatcher.repository.AgentSessionRepository;
import com.openai.agentwatcher.repository.AgentTelemetryRepository;
import com.openai.agentwatcher.repository.AgentThoughtRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;

/**
 * Service for watching and logging agent activities
 * Captures thoughts, actions, and telemetry data
 */
@Service
public class AgentWatcherService {

    private static final Logger log = LoggerFactory.getLogger(AgentWatcherService.class);

    @Autowired
    private AgentThoughtRepository thoughtRepository;

    @Autowired
    private AgentActionRepository actionRepository;

    @Autowired
    private AgentSessionRepository sessionRepository;

    @Autowired
    private AgentTelemetryRepository telemetryRepository;

    /**
     * Start a new agent session
     */
    @Transactional
    public AgentSession startSession(String agentId, String userQuery) {
        String sessionId = UUID.randomUUID().toString();
        
        AgentSession session = AgentSession.builder()
                .sessionId(sessionId)
                .agentId(agentId)
                .userQuery(userQuery)
                .sessionStatus("ACTIVE")
                .startTime(LocalDateTime.now())
                .totalThoughts(0)
                .totalActions(0)
                .build();
        
        session = sessionRepository.save(session);
        log.info("Started new agent session: {} for agent: {}", sessionId, agentId);
        
        return session;
    }

    /**
     * Log an agent thought
     */
    @Transactional
    public AgentThought logThought(String agentId, String sessionId, String thoughtType, 
                                   String thoughtContent, Double confidenceScore) {
        AgentThought thought = AgentThought.builder()
                .agentId(agentId)
                .sessionId(sessionId)
                .thoughtType(thoughtType)
                .thoughtContent(thoughtContent)
                .confidenceScore(confidenceScore)
                .timestamp(LocalDateTime.now())
                .build();
        
        thought = thoughtRepository.save(thought);
        
        // Update session thought count
        sessionRepository.findBySessionId(sessionId).ifPresent(session -> {
            session.incrementThoughts();
            sessionRepository.save(session);
        });
        
        log.info("Logged thought [{}] for session {}: {}", 
                thoughtType, sessionId, thoughtContent.substring(0, Math.min(50, thoughtContent.length())));
        
        return thought;
    }

    /**
     * Log an agent action start
     */
    @Transactional
    public AgentAction startAction(String agentId, String sessionId, String actionType, 
                                   String actionName, String inputData) {
        AgentAction action = AgentAction.builder()
                .agentId(agentId)
                .sessionId(sessionId)
                .actionType(actionType)
                .actionName(actionName)
                .inputData(inputData)
                .status("IN_PROGRESS")
                .startTime(LocalDateTime.now())
                .build();
        
        action = actionRepository.save(action);
        
        // Update session action count
        sessionRepository.findBySessionId(sessionId).ifPresent(session -> {
            session.incrementActions();
            sessionRepository.save(session);
        });
        
        log.info("Started action [{}] {} for session {}", actionType, actionName, sessionId);
        
        return action;
    }

    /**
     * Complete an agent action
     */
    @Transactional
    public void completeAction(Long actionId, String outputData) {
        actionRepository.findById(actionId).ifPresent(action -> {
            action.setOutputData(outputData);
            action.complete();
            actionRepository.save(action);
            log.info("Completed action {} for session {}", action.getActionName(), action.getSessionId());
        });
    }

    /**
     * Fail an agent action
     */
    @Transactional
    public void failAction(Long actionId, String errorMessage) {
        actionRepository.findById(actionId).ifPresent(action -> {
            action.fail(errorMessage);
            actionRepository.save(action);
            log.error("Action {} failed for session {}: {}", 
                    action.getActionName(), action.getSessionId(), errorMessage);
        });
    }

    /**
     * Log telemetry metric
     */
    @Transactional
    public void logTelemetry(String agentId, String sessionId, String metricName, 
                            Double metricValue, String metricUnit, String metricType) {
        AgentTelemetry telemetry = AgentTelemetry.builder()
                .agentId(agentId)
                .sessionId(sessionId)
                .metricName(metricName)
                .metricValue(metricValue)
                .metricUnit(metricUnit)
                .metricType(metricType)
                .timestamp(LocalDateTime.now())
                .build();
        
        telemetryRepository.save(telemetry);
        log.debug("Logged telemetry {}: {} {} for session {}", 
                metricName, metricValue, metricUnit, sessionId);
    }

    /**
     * Complete a session
     */
    @Transactional
    public void completeSession(String sessionId, String finalResponse) {
        sessionRepository.findBySessionId(sessionId).ifPresent(session -> {
            session.complete(finalResponse);
            sessionRepository.save(session);
            log.info("Completed session: {}", sessionId);
        });
    }

    /**
     * Get all thoughts for a session
     */
    public List<AgentThought> getSessionThoughts(String sessionId) {
        return thoughtRepository.findBySessionIdOrderByTimestampAsc(sessionId);
    }

    /**
     * Get all actions for a session
     */
    public List<AgentAction> getSessionActions(String sessionId) {
        return actionRepository.findBySessionIdOrderByStartTimeAsc(sessionId);
    }

    /**
     * Get session details
     */
    public AgentSession getSession(String sessionId) {
        return sessionRepository.findBySessionId(sessionId).orElse(null);
    }

    /**
     * Get all sessions for an agent
     */
    public List<AgentSession> getAgentSessions(String agentId) {
        return sessionRepository.findByAgentIdOrderByStartTimeDesc(agentId);
    }
}
