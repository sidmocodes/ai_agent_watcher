package com.openai.agentwatcher.service;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.Map;

/**
 * Service for parsing and interpreting OpenAI agent events
 * Extracts meaningful information from telemetry data
 */
@Service
public class AgentEventParserService {

    private static final Logger log = LoggerFactory.getLogger(AgentEventParserService.class);

    @Autowired
    private AgentWatcherService watcherService;

    private final ObjectMapper objectMapper = new ObjectMapper();

    /**
     * Parse and process an agent event
     */
    public void processAgentEvent(String agentId, String sessionId, Map<String, Object> eventData) {
        try {
            String eventType = (String) eventData.get("type");
            
            switch (eventType) {
                case "thought":
                    processThoughtEvent(agentId, sessionId, eventData);
                    break;
                case "action":
                    processActionEvent(agentId, sessionId, eventData);
                    break;
                case "tool_call":
                    processToolCallEvent(agentId, sessionId, eventData);
                    break;
                case "completion":
                    processCompletionEvent(agentId, sessionId, eventData);
                    break;
                case "error":
                    processErrorEvent(agentId, sessionId, eventData);
                    break;
                default:
                    log.warn("Unknown event type: {}", eventType);
            }
        } catch (Exception e) {
            log.error("Error processing agent event", e);
        }
    }

    private void processThoughtEvent(String agentId, String sessionId, Map<String, Object> eventData) {
        String thoughtType = (String) eventData.getOrDefault("thought_type", "GENERAL");
        String content = (String) eventData.get("content");
        Double confidence = eventData.containsKey("confidence") ? 
                ((Number) eventData.get("confidence")).doubleValue() : null;
        
        watcherService.logThought(agentId, sessionId, thoughtType, content, confidence);
        
        // Log thinking time as telemetry
        if (eventData.containsKey("processing_time_ms")) {
            Double processingTime = ((Number) eventData.get("processing_time_ms")).doubleValue();
            watcherService.logTelemetry(agentId, sessionId, "thinking_time", 
                    processingTime, "ms", "LATENCY");
        }
    }

    private void processActionEvent(String agentId, String sessionId, Map<String, Object> eventData) {
        String actionName = (String) eventData.get("action_name");
        String actionType = (String) eventData.getOrDefault("action_type", "GENERAL");
        String status = (String) eventData.get("status");
        
        if ("started".equals(status)) {
            String inputData = convertToJson(eventData.get("input"));
            watcherService.startAction(agentId, sessionId, actionType, actionName, inputData);
        } else if ("completed".equals(status)) {
            Long actionId = eventData.containsKey("action_id") ? 
                    ((Number) eventData.get("action_id")).longValue() : null;
            if (actionId != null) {
                String outputData = convertToJson(eventData.get("output"));
                watcherService.completeAction(actionId, outputData);
            }
        }
    }

    private void processToolCallEvent(String agentId, String sessionId, Map<String, Object> eventData) {
        String toolName = (String) eventData.get("tool_name");
        String status = (String) eventData.get("status");
        
        // Log as a thought about tool selection
        if ("selected".equals(status)) {
            String reasoning = (String) eventData.getOrDefault("reasoning", 
                    "Selected tool: " + toolName);
            watcherService.logThought(agentId, sessionId, "TOOL_SELECTION", reasoning, null);
        }
        
        // Log tool execution as an action
        String inputData = convertToJson(eventData.get("arguments"));
        watcherService.startAction(agentId, sessionId, "TOOL_USE", toolName, inputData);
    }

    private void processCompletionEvent(String agentId, String sessionId, Map<String, Object> eventData) {
        String finalResponse = (String) eventData.get("response");
        watcherService.completeSession(sessionId, finalResponse);
        
        // Log completion metrics
        if (eventData.containsKey("total_tokens")) {
            Double tokens = ((Number) eventData.get("total_tokens")).doubleValue();
            watcherService.logTelemetry(agentId, sessionId, "total_tokens", 
                    tokens, "tokens", "TOKENS");
        }
        
        if (eventData.containsKey("total_cost")) {
            Double cost = ((Number) eventData.get("total_cost")).doubleValue();
            watcherService.logTelemetry(agentId, sessionId, "total_cost", 
                    cost, "usd", "COST");
        }
    }

    private void processErrorEvent(String agentId, String sessionId, Map<String, Object> eventData) {
        String errorMessage = (String) eventData.get("error");
        Long actionId = eventData.containsKey("action_id") ? 
                ((Number) eventData.get("action_id")).longValue() : null;
        
        if (actionId != null) {
            watcherService.failAction(actionId, errorMessage);
        }
        
        // Log error as a thought
        watcherService.logThought(agentId, sessionId, "ERROR", errorMessage, 0.0);
        
        // Increment error count
        watcherService.logTelemetry(agentId, sessionId, "errors", 1.0, "count", "ERROR_RATE");
    }

    private String convertToJson(Object data) {
        if (data == null) {
            return null;
        }
        try {
            return objectMapper.writeValueAsString(data);
        } catch (Exception e) {
            log.error("Error converting to JSON", e);
            return data.toString();
        }
    }
}
