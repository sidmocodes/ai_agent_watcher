package com.openai.agentwatcher.controller;

import com.openai.agentwatcher.model.AgentAction;
import com.openai.agentwatcher.model.AgentSession;
import com.openai.agentwatcher.model.AgentThought;
import com.openai.agentwatcher.service.AgentWatcherService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;

/**
 * REST Controller for Agent Watcher endpoints
 */
@RestController
@RequestMapping("/sessions")
public class AgentSessionController {

    private static final Logger log = LoggerFactory.getLogger(AgentSessionController.class);

    @Autowired
    private AgentWatcherService watcherService;

    /**
     * Start a new agent session
     */
    @PostMapping("/start")
    public ResponseEntity<AgentSession> startSession(@RequestBody Map<String, String> request) {
        String agentId = request.get("agentId");
        String userQuery = request.get("userQuery");
        
        AgentSession session = watcherService.startSession(agentId, userQuery);
        return ResponseEntity.ok(session);
    }

    /**
     * Get session details
     */
    @GetMapping("/{sessionId}")
    public ResponseEntity<AgentSession> getSession(@PathVariable String sessionId) {
        AgentSession session = watcherService.getSession(sessionId);
        if (session == null) {
            return ResponseEntity.notFound().build();
        }
        return ResponseEntity.ok(session);
    }

    /**
     * Get all thoughts for a session
     */
    @GetMapping("/{sessionId}/thoughts")
    public ResponseEntity<List<AgentThought>> getSessionThoughts(@PathVariable String sessionId) {
        List<AgentThought> thoughts = watcherService.getSessionThoughts(sessionId);
        return ResponseEntity.ok(thoughts);
    }

    /**
     * Get all actions for a session
     */
    @GetMapping("/{sessionId}/actions")
    public ResponseEntity<List<AgentAction>> getSessionActions(@PathVariable String sessionId) {
        List<AgentAction> actions = watcherService.getSessionActions(sessionId);
        return ResponseEntity.ok(actions);
    }

    /**
     * Complete a session
     */
    @PostMapping("/{sessionId}/complete")
    public ResponseEntity<Void> completeSession(
            @PathVariable String sessionId, 
            @RequestBody Map<String, String> request) {
        String finalResponse = request.get("finalResponse");
        watcherService.completeSession(sessionId, finalResponse);
        return ResponseEntity.ok().build();
    }

    /**
     * Get all sessions for an agent
     */
    @GetMapping("/agent/{agentId}")
    public ResponseEntity<List<AgentSession>> getAgentSessions(@PathVariable String agentId) {
        List<AgentSession> sessions = watcherService.getAgentSessions(agentId);
        return ResponseEntity.ok(sessions);
    }
}
