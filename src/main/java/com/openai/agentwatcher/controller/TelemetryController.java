package com.openai.agentwatcher.controller;

import com.openai.agentwatcher.service.AgentWatcherService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.Map;

/**
 * REST Controller for receiving telemetry events
 */
@RestController
@RequestMapping("/telemetry")
@Slf4j
public class TelemetryController {

    @Autowired
    private AgentWatcherService watcherService;

    /**
     * Submit a generic telemetry event
     */
    @PostMapping("/events")
    public ResponseEntity<String> submitEvent(@RequestBody Map<String, Object> event) {
        String agentId = (String) event.get("agentId");
        String sessionId = (String) event.get("sessionId");
        String type = (String) event.get("type");
        
        log.info("Received telemetry event: type={}, agent={}, session={}", type, agentId, sessionId);
        
        if ("thought".equals(type)) {
            String thoughtType = (String) event.getOrDefault("thought_type", "GENERAL");
            String content = (String) event.get("content");
            Double confidence = event.containsKey("confidence") ? 
                    ((Number) event.get("confidence")).doubleValue() : null;
            
            watcherService.logThought(agentId, sessionId, thoughtType, content, confidence);
        } else if ("action".equals(type)) {
            String actionType = (String) event.getOrDefault("action_type", "GENERAL");
            String actionName = (String) event.get("action_name");
            String inputData = event.containsKey("input_data") ? 
                    event.get("input_data").toString() : null;
            
            watcherService.startAction(agentId, sessionId, actionType, actionName, inputData);
        } else if ("metric".equals(type)) {
            String metricName = (String) event.get("metric_name");
            Double metricValue = event.containsKey("metric_value") ? 
                    ((Number) event.get("metric_value")).doubleValue() : null;
            String metricUnit = (String) event.getOrDefault("metric_unit", "");
            String metricType = (String) event.getOrDefault("metric_type", "CUSTOM");
            
            watcherService.logTelemetry(agentId, sessionId, metricName, metricValue, metricUnit, metricType);
        }
        
        return ResponseEntity.ok("Event received");
    }

    /**
     * Log a thought
     */
    @PostMapping("/thoughts")
    public ResponseEntity<String> logThought(@RequestBody Map<String, Object> request) {
        String agentId = (String) request.get("agentId");
        String sessionId = (String) request.get("sessionId");
        String thoughtType = (String) request.get("thoughtType");
        String content = (String) request.get("content");
        Double confidence = request.containsKey("confidence") ? 
                ((Number) request.get("confidence")).doubleValue() : null;
        
        watcherService.logThought(agentId, sessionId, thoughtType, content, confidence);
        return ResponseEntity.ok("Thought logged");
    }

    /**
     * Log an action
     */
    @PostMapping("/actions")
    public ResponseEntity<String> logAction(@RequestBody Map<String, Object> request) {
        String agentId = (String) request.get("agentId");
        String sessionId = (String) request.get("sessionId");
        String actionType = (String) request.get("actionType");
        String actionName = (String) request.get("actionName");
        String inputData = request.containsKey("inputData") ? 
                request.get("inputData").toString() : null;
        
        watcherService.startAction(agentId, sessionId, actionType, actionName, inputData);
        return ResponseEntity.ok("Action logged");
    }

    /**
     * Log a metric
     */
    @PostMapping("/metrics")
    public ResponseEntity<String> logMetric(@RequestBody Map<String, Object> request) {
        String agentId = (String) request.get("agentId");
        String sessionId = (String) request.get("sessionId");
        String metricName = (String) request.get("metricName");
        Double metricValue = request.containsKey("metricValue") ? 
                ((Number) request.get("metricValue")).doubleValue() : null;
        String metricUnit = (String) request.getOrDefault("metricUnit", "");
        String metricType = (String) request.getOrDefault("metricType", "CUSTOM");
        
        watcherService.logTelemetry(agentId, sessionId, metricName, metricValue, metricUnit, metricType);
        return ResponseEntity.ok("Metric logged");
    }
}